package br.com.setupshop.customer.application.usecase.update;

import br.com.setupshop.customer.domain.exception.CustomerNotFoundException;
import br.com.setupshop.customer.domain.exception.EmailAlreadyExistsException;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;

    public UpdateCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer execute(Long customerId, UpdateCustomerCommand command) {

        boolean noFieldsProvided =
            command == null ||
                (command.name() == null &&
                    command.email() == null &&
                    command.phone() == null);

        if (noFieldsProvided) {
            throw new IllegalArgumentException("At least one field must be provided");
        }

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        String emailCandidate = customer.normalizeEmailCandidate(command.email());
        boolean emailChanged = !customer.getEmail().equals(emailCandidate);

        if (emailChanged) {
            boolean emailAlreadyExists = customerRepository.existsByEmail(emailCandidate);

            if (emailAlreadyExists) {
                throw new EmailAlreadyExistsException(emailCandidate);
            }
        }

        customer.updateDetails(command.name(), command.email(), command.phone());

        return customerRepository.save(customer);
    }
}
