package com.javacodeex.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long orderId,
        String productCode,
        Integer quantity,
        BigDecimal price,
        String status,
        LocalDateTime createdAt
) {
}
