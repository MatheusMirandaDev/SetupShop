package br.com.setupshop.order.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Order item data returned by the API")
public record OrderItemResponse(

    @Schema(
        description = "Product identifier",
        example = "1"
    )
    Long productId,

    @Schema(
        description = "Product quantity",
        example = "2"
    )
    int quantity,

    @Schema(
        description = "Product unit price captured when the order was placed",
        example = "300.00"
    )
    BigDecimal unitPrice,

    @Schema(
        description = "Item subtotal (unit price multiplied by quantity)",
        example = "600.00"
    )
    BigDecimal subtotal
) {
}
