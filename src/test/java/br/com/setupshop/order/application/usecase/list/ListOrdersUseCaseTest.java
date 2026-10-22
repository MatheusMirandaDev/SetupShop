package br.com.setupshop.order.application.usecase.list;

import br.com.setupshop.order.domain.model.Order;
import br.com.setupshop.order.domain.model.OrderItem;
import br.com.setupshop.order.domain.repository.OrderRepository;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListOrdersUseCaseTest {

    private OrderRepository orderRepository;
    private ListOrdersUseCase listOrdersUseCase;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        listOrdersUseCase = new ListOrdersUseCase(orderRepository);
    }

    @Test
    void shouldReturnPaginatedOrders() {

        OrderItem item1 = new OrderItem(2L, 2, new BigDecimal("300.00"));
        Order order1 = new Order(1L, List.of(item1));

        OrderItem item2 = new OrderItem(7L, 3, new BigDecimal("140.00"));
        Order order2 = new Order(1L, List.of(item2));

        PageQuery pageQuery = new PageQuery(0, 10);
        PageResult<Order> pageResult = new PageResult<>(
            List.of(order1, order2), 0, 10, 2L, 1);

        when(orderRepository.findAll(pageQuery)).thenReturn(pageResult);

        var result = listOrdersUseCase.execute(pageQuery);

        assertEquals(2, result.content().size());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(2L, result.totalElements());
        assertEquals(1, result.totalPages());
        assertSame(order1, result.content().get(0));
        assertSame(order2, result.content().get(1));

        verify(orderRepository).findAll(pageQuery);
    }
}
