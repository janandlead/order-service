package com.javacodeex.order.dto;

/**
 * Inventory details returned by the inventory service.
 *
 * @param productCode code identifying the checked product
 * @param productName name of the checked product
 * @param availableQuantity number of units available in inventory
 * @param available whether inventory can satisfy the requested quantity
 */
public record InventoryResponse(
        String productCode,
        String productName,
        Integer availableQuantity,
        boolean available
) {
}
