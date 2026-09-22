package br.com.setupshop.customer.application.usecase.deactivate;

import br.com.setupshop.customer.domain.exception.CustomerNotFoundException;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeactivateCustomerUseCase {

    private final CustomerRepository customerRepository;

    public DeactivateCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public void execute(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException(id));
        customer.deactivate();
        customerRepository.save(customer);
    }
}
