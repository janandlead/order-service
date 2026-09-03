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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public OrderService(OrderRepository orderRepository, InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
    }

    public OrderResponse placeOrder(OrderRequest request) {
        InventoryResponse inventoryResponse;
        try {
            inventoryResponse = inventoryClient.checkStock(request.productCode(), request.quantity());
        } catch (FeignException exception) {
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
}
