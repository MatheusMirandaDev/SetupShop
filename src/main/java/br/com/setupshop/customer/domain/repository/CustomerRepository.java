package br.com.setupshop.customer.domain.repository;

import br.com.setupshop.customer.domain.model.Customer;

public interface CustomerRepository {

    Customer save(Customer customer);

    boolean existsByEmail(String email);
}
