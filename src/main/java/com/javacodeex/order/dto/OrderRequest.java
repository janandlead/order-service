package com.javacodeex.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Input for placing an order, validated at the HTTP controller boundary.
 *
 * @param productCode nonblank code identifying the product to order
 * @param quantity nonnull, positive number of units to order
 * @param price nonnull, positive price supplied for the order
 */
public record OrderRequest(
        @NotBlank String productCode,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal price
) {
}
