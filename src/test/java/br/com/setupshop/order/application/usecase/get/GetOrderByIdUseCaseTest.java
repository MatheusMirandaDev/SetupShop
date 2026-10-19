package br.com.setupshop.order.application.usecase.get;

import br.com.setupshop.order.domain.exception.OrderNotFoundException;
import br.com.setupshop.order.domain.model.Order;
import br.com.setupshop.order.domain.model.OrderItem;
import br.com.setupshop.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetOrderByIdUseCaseTest {

    private OrderRepository orderRepository;
    private GetOrderByIdUseCase getOrderByIdUseCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        getOrderByIdUseCase = new GetOrderByIdUseCase(orderRepository);
    }

    @Test
    void shouldReturnOrderWhenFound() {
        Long orderId = 1L;

        OrderItem item = new OrderItem(2L, 2, new BigDecimal("140.00"));
        Order order = new Order(1L, List.of(item));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        var result = getOrderByIdUseCase.execute(orderId);

        assertSame(order, result);
        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {
        Long orderId = 1L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        var exception = assertThrows(
            OrderNotFoundException.class,
            () -> getOrderByIdUseCase.execute(orderId)
        );

        assertEquals("Order not found with id: " + orderId, exception.getMessage());
        verify(orderRepository).findById(orderId);
    }
}
