package br.com.setupshop.order.domain.repository;

import br.com.setupshop.order.domain.model.Order;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    PageResult<Order> findAll(PageQuery pageQuery);
}
