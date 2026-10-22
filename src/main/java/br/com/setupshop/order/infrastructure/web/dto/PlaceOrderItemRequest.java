package br.com.setupshop.order.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Data required for an order item")
public record PlaceOrderItemRequest(

    @Schema(
        description = "Product identifier",
        example = "1"
    )
    @NotNull
    @Positive
    Long productId,

    @Schema(
        description = "Product quantity",
        example = "2"
    )
    @Min(1)
    int quantity
) {
}
