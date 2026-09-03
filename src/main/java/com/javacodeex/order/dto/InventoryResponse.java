package com.javacodeex.order.dto;

public record InventoryResponse(
        String productCode,
        String productName,
        Integer availableQuantity,
        boolean available
) {
}
