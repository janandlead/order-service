package com.javacodeex.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Persisted order details returned after an order is placed.
 *
 * @param orderId database-generated order identifier
 * @param productCode code identifying the ordered product
 * @param quantity number of units ordered
 * @param price price stored for the order
 * @param status order processing status
 * @param createdAt local date and time when the order was created
 */
public record OrderResponse(
        Long orderId,
        String productCode,
        Integer quantity,
        BigDecimal price,
        String status,
        LocalDateTime createdAt
) {
}
