package br.com.setupshop.order.domain.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = {@JoinColumn(name = "order_id")})
    private List<OrderItem> orderItems;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private Instant createdAt;

    public List<OrderItem> getOrderItems() {
        return List.copyOf(orderItems);
    }

    public BigDecimal total() {
        return total;
    }

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
}
