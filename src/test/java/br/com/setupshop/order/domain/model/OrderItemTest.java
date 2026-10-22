package br.com.setupshop.order.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    @Test
    void shouldCreateOrderItemSubtotalValue() {
        Long productId = 12L;
        int itemQuantity = 2;
        BigDecimal price = new BigDecimal("140.00");

        OrderItem orderItem = new OrderItem(productId, itemQuantity, price);

        assertEquals(new BigDecimal("280.00"), orderItem.subtotal());
    }

    @Test
    void shouldPreserveProductId() {
        Long productId = 12L;
        int itemQuantity = 2;
        BigDecimal price = new BigDecimal("140.00");

        OrderItem orderItem = new OrderItem(productId, itemQuantity, price);

        assertEquals(productId, orderItem.getProductId());
    }

    @Test
    void shouldRejectOrderItemWithZeroQuantity() {
        Long productId = 12L;
        int itemQuantity = 0;
        BigDecimal price = new BigDecimal("140.00");

        assertThrows(IllegalArgumentException.class, () -> new OrderItem(productId, itemQuantity, price));
    }

    @Test
    void shouldRejectOrderItemWithNullProductId() {
        Long productId = null;
        int itemQuantity = 2;
        BigDecimal price = new BigDecimal("140.00");

        assertThrows(IllegalArgumentException.class, () -> new OrderItem(productId, itemQuantity, price));
    }

    @Test
    void shouldRejectOrderItemWithNullUnitPrice() {
        Long productId = 12L;
        int itemQuantity = 2;
        BigDecimal price = null;
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(productId, itemQuantity, price));
    }

    @Test
    void shouldRejectOrderItemWithUnitPriceMoreTwoDecimalPlaces() {
        Long productId = 12L;
        int itemQuantity = 2;
        BigDecimal price = new BigDecimal("140.123");
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(productId, itemQuantity, price));
    }
}
