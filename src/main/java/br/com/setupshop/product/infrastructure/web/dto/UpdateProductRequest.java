package br.com.setupshop.product.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @Size(max = 200)
        String name,

        @Size(max = 500)
        String description,

        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal price
) {
}
