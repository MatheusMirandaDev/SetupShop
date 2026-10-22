package br.com.setupshop.order.application.usecase.place;

public record PlaceOrderItemCommand(
    Long productId,
    int quantity
) {
}
