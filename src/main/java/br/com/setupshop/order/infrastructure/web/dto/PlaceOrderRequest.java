package br.com.setupshop.order.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "Data required to place an order")
public record PlaceOrderRequest(

    @Schema(
        description = "Customer identifier",
        example = "1"
    )
    @NotNull
    @Positive
    Long customerId,

    @Schema(
        description = "Items included in the order"
    )
    @NotEmpty
    List<@NotNull @Valid PlaceOrderItemRequest> items
) {
}
