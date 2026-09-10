package br.com.setupshop.customer.infrastructure.persistence;

import br.com.setupshop.customer.domain.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);
}
