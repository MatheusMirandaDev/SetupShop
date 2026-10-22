package br.com.setupshop.order.infrastructure.persistence;

import br.com.setupshop.order.domain.model.Order;
import br.com.setupshop.order.domain.repository.OrderRepository;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Override
    public PageResult<Order> findAll(PageQuery pageQuery) {
        PageRequest pageRequest =
            PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by("id").ascending());

        var resultPage = orderRepository.findAll(pageRequest);

        return new PageResult<>(
            resultPage.getContent(),
            resultPage.getNumber(),
            resultPage.getSize(),
            resultPage.getTotalElements(),
            resultPage.getTotalPages()
        );
    }
}
