package br.com.setupshop.product.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Fields used to partially update a product. At least one field must be provided.")
public record UpdateProductRequest(

    @Schema(
        description = "New product name. Omit to keep the current name.",
        example = "Keyboard AULA F75"
    )
    @Size(min = 1, max = 200)
    String name,

    @Schema(
        description = "New product description. Omit to keep the current description; send a blank value to remove it.",
        example = "Wireless mechanical keyboard"
    )
    @Size(max = 500)
    String description,

    @Schema(
        description = "New non-negative product price with at most two decimal places. Omit to keep the current price.",
        example = "300.00"
    )
    @DecimalMin(value = "0.00", inclusive = true)
    BigDecimal price
) {
}
