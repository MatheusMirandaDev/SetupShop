package br.com.setupshop.product.infrastructure.web.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank
        @Size(max = 200)
         String name,

        @Size(max = 500)
        String description,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal price
) {
}
