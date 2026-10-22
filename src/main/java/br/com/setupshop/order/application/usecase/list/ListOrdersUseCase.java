package br.com.setupshop.order.application.usecase.list;

import br.com.setupshop.order.domain.model.Order;
import br.com.setupshop.order.domain.repository.OrderRepository;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListOrdersUseCase {

    private final OrderRepository orderRepository;

    public ListOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<Order> execute(PageQuery pageQuery) {
        return orderRepository.findAll(pageQuery);
    }
}
