package br.com.setupshop.order.application.usecase.place;

import java.util.List;

public record PlaceOrderCommand(
    Long customerId,
    List<PlaceOrderItemCommand> items
) {
}
