package br.com.setupshop.order.infrastructure.persistence;

import br.com.setupshop.order.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderRepository extends JpaRepository<Order, Long> {
}
