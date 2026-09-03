package com.javacodeex.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderRequest(
        @NotBlank String productCode,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal price
) {
}
