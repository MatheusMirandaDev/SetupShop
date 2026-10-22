package br.com.setupshop.order.application.usecase.get;

import br.com.setupshop.order.domain.exception.OrderNotFoundException;
import br.com.setupshop.order.domain.model.Order;
import br.com.setupshop.order.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetOrderByIdUseCase {

    private final OrderRepository orderRepository;

    public GetOrderByIdUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Order execute(Long orderId) {
        return orderRepository
            .findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
