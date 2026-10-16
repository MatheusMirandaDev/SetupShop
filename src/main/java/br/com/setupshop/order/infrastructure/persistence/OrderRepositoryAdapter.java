package br.com.setupshop.order.infrastructure.persistence;

import br.com.setupshop.order.domain.model.Order;
import br.com.setupshop.order.domain.repository.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {

    private final JpaOrderRepository orderRepository;

    public OrderRepositoryAdapter(JpaOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
}
