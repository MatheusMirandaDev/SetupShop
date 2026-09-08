package br.com.setupshop.customer.application.usecase.create;

import br.com.setupshop.customer.domain.exception.EmailAlreadyExistsException;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateCustomerUseCase {

    private final CustomerRepository customerRepository;

    public CreateCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer execute(CreateCustomerCommand command) {
        Customer customer = new Customer(
            command.name(),
            command.email(),
            command.phone()
        );

        boolean emailAlreadyExists = customerRepository.existsByEmail(customer.getEmail());

        if (emailAlreadyExists) {
            throw new EmailAlreadyExistsException(customer.getEmail());
        }

        return customerRepository.save(customer);
    }
}
