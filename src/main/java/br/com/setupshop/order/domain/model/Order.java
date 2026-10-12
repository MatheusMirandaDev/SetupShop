package br.com.setupshop.order.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter

public class Order {

    private Long id;

    private Long customerId;

    private List<OrderItem> orderItems;

    private OrderStatus status;

    private BigDecimal total;

    public Order(Long customerId, List<OrderItem> orderItems) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be valid");
        }
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("Order items must not be empty");
        }
        if (orderItems.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Order items must not be null");
        }

        this.customerId = customerId;
        this.orderItems = new ArrayList<>(orderItems);
        this.status = OrderStatus.CREATED;
        this.total = orderItems.stream()
            .map(OrderItem::subtotal)
            .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    public List<OrderItem> getOrderItems() {
        return List.copyOf(orderItems);
    }

    public BigDecimal total() {
        return total;
    }
}
