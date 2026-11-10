package br.com.setupshop.account.application.usecase.register;

import br.com.setupshop.account.domain.exception.AccountAlreadyExistsException;
import br.com.setupshop.account.domain.exception.AccountEmailAlreadyExistsException;
import br.com.setupshop.account.domain.model.Account;
import br.com.setupshop.account.domain.model.AccountRole;
import br.com.setupshop.account.domain.repository.AccountRepository;
import br.com.setupshop.customer.domain.exception.EmailAlreadyExistsException;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterCustomerAccountUseCase {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public RegisterCustomerAccountUseCase(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Account execute(RegisterCustomerAccountCommand command) {

        var existingAccount = accountRepository.
            findByIssuerAndSubject(command.issuer(), command.subject());
        if (existingAccount.isPresent()) {
            throw new AccountAlreadyExistsException();
        }

        Customer customer = new Customer(
            command.name(),
            command.email(),
            command.phone()
        );
        boolean emailCustomerAlreadyExists = customerRepository.existsByEmail(customer.getEmail());
        if (emailCustomerAlreadyExists) {
            throw new EmailAlreadyExistsException(customer.getEmail());
        }

        boolean emailAccountAlreadyExists = accountRepository.existsByEmail(customer.getEmail());
        if (emailAccountAlreadyExists) {
            throw new AccountEmailAlreadyExistsException(customer.getEmail());
        }

        Customer savedCustomer = customerRepository.save(customer);

        Account account = new Account(
            command.email(),
            savedCustomer.getId(),
            AccountRole.CUSTOMER,
            command.issuer(),
            command.subject()
        );

        return accountRepository.save(account);
    }
}
