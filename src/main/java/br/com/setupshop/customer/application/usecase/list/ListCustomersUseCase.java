package br.com.setupshop.customer.application.usecase.list;

import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListCustomersUseCase {

    private final CustomerRepository customerRepository;

    public ListCustomersUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<Customer> execute(PageQuery pageQuery) {
        return customerRepository.findAll(pageQuery);
    }
}
