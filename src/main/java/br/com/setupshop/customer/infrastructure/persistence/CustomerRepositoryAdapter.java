package br.com.setupshop.customer.infrastructure.persistence;

import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final JpaCustomerRepository customerRepository;

    public CustomerRepositoryAdapter(JpaCustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public PageResult<Customer> findAll(PageQuery pageQuery) {

        PageRequest pageRequest =
            PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by("id").ascending());

        var resultPage = customerRepository.findAll(pageRequest);

        return new PageResult<>(
            resultPage.getContent(),
            resultPage.getNumber(),
            resultPage.getSize(),
            resultPage.getTotalElements(),
            resultPage.getTotalPages()
        );
    }
}
