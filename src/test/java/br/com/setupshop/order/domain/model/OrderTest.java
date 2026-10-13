package br.com.setupshop.order.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    void shouldCreateOrderWithValidData() {
        Long customerId = 1L;

        OrderItem orderItem1 = new OrderItem(1L, 2, new BigDecimal("140.00"));
        OrderItem orderItem2 = new OrderItem(2L, 3, new BigDecimal("10.00"));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem1);
        orderItems.add(orderItem2);

        Order order = new Order(customerId, orderItems);

        assertEquals(customerId, order.getCustomerId());
        assertEquals(orderItems, order.getOrderItems());
        assertEquals(new BigDecimal("310.00"), order.total());
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    void shouldRejectOrderWithNullCustomerId() {
        Long customerId = null;
        OrderItem orderItem1 = new OrderItem(1L, 2, new BigDecimal("140.00"));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem1);

        assertThrows(IllegalArgumentException.class, () -> new Order(customerId, orderItems));
    }

    @Test
    void shouldRejectOrderWithEmptyItems() {
        Long customerId = 1L;
        List<OrderItem> orderItems = new ArrayList<>();

        assertThrows(IllegalArgumentException.class, () -> new Order(customerId, orderItems));
    }

    @Test
    void shouldNotChangeOrderWhenOriginalItemsListChanges() {
        Long customerId = 1L;
        OrderItem orderItem1 = new OrderItem(1L, 2, new BigDecimal("140.00"));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem1);

        Order order = new Order(customerId, orderItems);

        OrderItem orderItem2 = new OrderItem(2L, 3, new BigDecimal("10.00"));
        orderItems.add(orderItem2);

        assertEquals(1, order.getOrderItems().size());
        assertEquals(new BigDecimal("280.00"), order.total());
    }

    @Test
    void shouldRejectOrderWithNullItem() {
        Long customerId = 1L;
        OrderItem orderItem = new OrderItem(1L, 2, new BigDecimal("140.00"));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        orderItems.add(null);

        assertThrows(IllegalArgumentException.class, () -> new Order(customerId, orderItems));
    }

    @Test
    void shouldKeepItemsImmutableToCallers() {
        Order order = new Order(1L, List.of(new OrderItem(1L, 1, new BigDecimal("10.00"))));

        assertThrows(UnsupportedOperationException.class, () ->
            order.getOrderItems().add(new OrderItem(2L, 1, new BigDecimal("20.00"))));
        assertEquals(new BigDecimal("10.00"), order.getTotal());
    }
}
