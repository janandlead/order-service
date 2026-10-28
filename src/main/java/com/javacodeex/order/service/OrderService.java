package com.javacodeex.order.service;

import com.javacodeex.order.client.InventoryClient;
import com.javacodeex.order.dto.InventoryResponse;
import com.javacodeex.order.dto.OrderRequest;
import com.javacodeex.order.dto.OrderResponse;
import com.javacodeex.order.entity.Order;
import com.javacodeex.order.exception.InsufficientStockException;
import com.javacodeex.order.exception.InventoryServiceUnavailableException;
import com.javacodeex.order.repository.OrderRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    /**
     * Creates the service with order persistence and inventory lookup dependencies.
     *
     * @param orderRepository repository used to save orders
     * @param inventoryClient client used to check product availability
     */
    public OrderService(OrderRepository orderRepository, InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
    }

    /**
     * Checks inventory and persists a confirmed order when sufficient stock is available.
     * The inventory check does not reserve or deduct stock. Calls through the Spring
     * proxy use the configured inventory retry and circuit-breaker policies.
     *
     * @param request product, quantity, and price details for the order
     * @return the saved order's identifier, details, status, and creation time
     * @throws InsufficientStockException if the product is missing or stock is insufficient
     * @throws InventoryServiceUnavailableException if inventory is unavailable or
     *         the circuit-breaker fallback handles a failure
     */
    @Retry(name = "inventory-service")
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "inventoryFallback")
    public OrderResponse placeOrder(OrderRequest request) {
        log.info("Checking inventory for productCode={} quantity={}",
                request.productCode(), request.quantity());

        InventoryResponse inventoryResponse;
        try {
            inventoryResponse = inventoryClient.checkStock(request.productCode(), request.quantity());
        } catch (FeignException exception) {
            log.warn("Inventory check failed for productCode={} with HTTP status={}",
                    request.productCode(), exception.status());
            if (exception.status() == 404) {
                throw new InsufficientStockException("Product not found: " + request.productCode());
            }
            throw new InventoryServiceUnavailableException(
                    "Inventory service is currently unavailable", exception);
        }

        if (!inventoryResponse.available()) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + request.productCode());
        }

        Order order = new Order(
                request.productCode(),
                request.quantity(),
                request.price(),
                "CONFIRMED",
                LocalDateTime.now()
        );
        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    /**
     * Maps a persisted order to its API response representation.
     *
     * @param order order whose fields will be copied
     * @return a response containing the order's stored details
     */
    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getProductCode(),
                order.getQuantity(),
                order.getPrice(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    /**
     * Logs a circuit-breaker fallback and propagates an inventory-unavailable error.
     * An existing inventory-unavailable exception is preserved; other failures are
     * wrapped with their original cause.
     *
     * @param request order request associated with the failed call
     * @param throwable failure that triggered the fallback
     * @return never returns normally; the type matches the protected method
     * @throws InventoryServiceUnavailableException always, to report the failed check
     */
    private OrderResponse inventoryFallback(OrderRequest request, Throwable throwable) {
        log.error("Inventory fallback invoked for productCode={} after retries/circuit-breaker rejection: {}",
                request.productCode(), throwable.toString());
        if (throwable instanceof InventoryServiceUnavailableException unavailableException) {
            throw unavailableException;
        }
        throw new InventoryServiceUnavailableException(
                "Inventory service is currently unavailable", throwable);
    }
}
