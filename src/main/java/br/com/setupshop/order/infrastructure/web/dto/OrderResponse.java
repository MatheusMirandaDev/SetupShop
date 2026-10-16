package br.com.setupshop.order.infrastructure.web.dto;

import br.com.setupshop.order.domain.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Order data returned by the API")
public record OrderResponse(

    @Schema(
        description = "Order identifier",
        example = "1"
    )
    Long id,

    @Schema(
        description = "Customer identifier",
        example = "1"
    )
    Long customerId,

    @Schema(
        description = "Items included in the order"
    )
    List<OrderItemResponse> items,

    @Schema(
        description = "Order status",
        example = "CREATED"
    )
    OrderStatus status,

    @Schema(
        description = "Non-negative total price with at most two decimal places",
        example = "1200.00"
    )
    BigDecimal total
) {
}
