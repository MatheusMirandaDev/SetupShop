package br.com.setupshop.order.domain.repository;

import br.com.setupshop.order.domain.model.Order;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);
}
