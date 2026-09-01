package br.com.setupshop.product.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Data required to create a product")
public record CreateProductRequest(
    @Schema(
        description = "Product name",
        example = "Keyboard AULA F75"
    )
    @NotBlank
    @Size(max = 200)
    String name,

    @Schema(
        description = "Optional product description",
        example = "Wireless mechanical keyboard"
    )
    @Size(max = 500)
    String description,

    @Schema(
        description = "Non-negative product price with at most two decimal places",
        example = "300.00"
    )
    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    BigDecimal price
) {
}
