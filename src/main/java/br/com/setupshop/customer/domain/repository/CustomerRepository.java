package br.com.setupshop.customer.domain.repository;

import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;

import java.util.Optional;

public interface CustomerRepository {

    Customer save(Customer customer);

    boolean existsByEmail(String email);

    Optional<Customer> findById(Long id);

    PageResult<Customer> findAll(PageQuery pageQuery);
}
