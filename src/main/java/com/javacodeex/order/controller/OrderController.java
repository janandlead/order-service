package com.javacodeex.order.controller;

import com.javacodeex.order.dto.OrderRequest;
import com.javacodeex.order.dto.OrderResponse;
import com.javacodeex.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates the controller with the service responsible for placing orders.
     *
     * @param orderService order processing service
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Handles an order submission and returns the created order with HTTP 201.
     * Spring validates the request body before invoking this method.
     *
     * @param request validated product, quantity, and price details
     * @return the persisted order details
     * @throws com.javacodeex.order.exception.InsufficientStockException if the product
     *         does not exist or its stock is insufficient
     * @throws com.javacodeex.order.exception.InventoryServiceUnavailableException
     *         if the inventory check cannot be completed
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.placeOrder(request);
    }
}
