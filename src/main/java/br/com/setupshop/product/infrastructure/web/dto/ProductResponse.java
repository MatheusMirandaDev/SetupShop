package br.com.setupshop.product.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Product data returned by the API")
public record ProductResponse(

    @Schema(
        description = "Product identifier",
        example = "1"
    )
    Long id,

    @Schema(
        description = "Product name",
        example = "Keyboard AULA F75"
    )
    String name,

    @Schema(
        description = "Product description",
        example = "Wireless mechanical keyboard"
    )
    String description,

    @Schema(
        description = "Product price",
        example = "300.00"
    )
    BigDecimal price,

    @Schema(
        description = "Indicates whether the product is active",
        example = "true"
    )
    boolean active
) {
}
