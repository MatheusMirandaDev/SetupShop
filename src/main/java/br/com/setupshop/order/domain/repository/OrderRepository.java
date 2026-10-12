package br.com.setupshop.order.domain.repository;

import br.com.setupshop.order.domain.model.Order;

public interface OrderRepository {

    Order save(Order order);
}
