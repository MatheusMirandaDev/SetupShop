package br.com.setupshop.account.application.usecase.register;

import br.com.setupshop.account.domain.exception.AccountAlreadyExistsException;
import br.com.setupshop.account.domain.exception.AccountEmailAlreadyExistsException;
import br.com.setupshop.account.domain.model.Account;
import br.com.setupshop.account.domain.model.AccountRole;
import br.com.setupshop.account.domain.repository.AccountRepository;
import br.com.setupshop.customer.domain.exception.EmailAlreadyExistsException;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RegisterCustomerAccountUseCaseTest {

    private AccountRepository accountRepository;
    private CustomerRepository customerRepository;
    private RegisterCustomerAccountUseCase registerCustomerAccountUseCase;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        customerRepository = mock(CustomerRepository.class);
        registerCustomerAccountUseCase =
            new RegisterCustomerAccountUseCase(accountRepository, customerRepository);
    }

    @Test
    void shouldCreateCustomerAndAccountForExternalIdentity() {

        //  Email already verified by a trusted backend integration
        String email = "matheus.miranda@gmail.com";
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";
        String name = "Matheus Miranda";
        String phone = "61999999999";

        when(accountRepository.findByIssuerAndSubject(issuer, subject))
            .thenReturn(Optional.empty());

        when(customerRepository.existsByEmail(email)).thenReturn(false);

        Customer savedCustomer = mock(Customer.class);

        when(savedCustomer.getId()).thenReturn(10L);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterCustomerAccountCommand command =
            new RegisterCustomerAccountCommand(
                issuer,
                subject,
                email,
                name,
                phone
            );

        var result = registerCustomerAccountUseCase.execute(command);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        var capturedCustomer = captor.getValue();

        assertTrue(capturedCustomer.isActive());
        assertEquals(name, capturedCustomer.getName());
        assertEquals(email, capturedCustomer.getEmail());
        assertEquals(phone, capturedCustomer.getPhone());
        assertEquals(10L, result.getCustomerId());
        assertEquals(AccountRole.CUSTOMER, result.getRole());
        assertEquals(email, result.getEmail());
        assertEquals(issuer, result.getIssuer());
        assertEquals(subject, result.getSubject());
        verify(accountRepository).save(result);
    }

    @Test
    void shouldRejectRegistrationWhenExternalIdentityAlreadyExists() {

        String email = "matheus.miranda@gmail.com";
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";
        String name = "Matheus Miranda";
        String phone = "61999999999";

        RegisterCustomerAccountCommand command =
            new RegisterCustomerAccountCommand(
                issuer,
                subject,
                email,
                name,
                phone
            );

        Account account =
            new Account(
                command.email(),
                10L,
                AccountRole.CUSTOMER,
                command.issuer(),
                command.subject()
            );

        when(accountRepository.findByIssuerAndSubject(issuer, subject))
            .thenReturn(Optional.of(account));

        var exception = assertThrows(AccountAlreadyExistsException.class,
            () -> registerCustomerAccountUseCase.execute(command));

        assertEquals("Account already exists for external identity", exception.getMessage());
        verify(accountRepository).findByIssuerAndSubject(issuer, subject);
        verifyNoInteractions(customerRepository);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldRejectRegistrationWhenCustomerEmailAlreadyExists() {
        String email = "   MATHEUS.miranda@gmail.com   ";
        String normalizedEmail = "matheus.miranda@gmail.com";
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";
        String name = "Matheus Miranda";
        String phone = "61999999999";

        RegisterCustomerAccountCommand command =
            new RegisterCustomerAccountCommand(
                issuer,
                subject,
                email,
                name,
                phone
            );

        when(accountRepository.findByIssuerAndSubject(issuer, subject))
            .thenReturn(Optional.empty());

        when(customerRepository.existsByEmail(normalizedEmail)).thenReturn(true);

        var exception = assertThrows(EmailAlreadyExistsException.class,
            () -> registerCustomerAccountUseCase.execute(command));

        assertEquals("Customer email already exists: " + normalizedEmail,
            exception.getMessage());
        verify(accountRepository).findByIssuerAndSubject(issuer, subject);
        verify(customerRepository).existsByEmail(normalizedEmail);
        verify(accountRepository, never()).save(any(Account.class));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldRejectRegistrationWhenAccountEmailAlreadyExists() {
        String email = "   MATHEUS.miranda@gmail.com   ";
        String normalizedEmail = "matheus.miranda@gmail.com";
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";
        String name = "Matheus Miranda";
        String phone = "61999999999";

        RegisterCustomerAccountCommand command =
            new RegisterCustomerAccountCommand(
                issuer,
                subject,
                email,
                name,
                phone
            );

        when(accountRepository.findByIssuerAndSubject(issuer, subject))
            .thenReturn(Optional.empty());

        when(customerRepository.existsByEmail(normalizedEmail)).thenReturn(false);
        when(accountRepository.existsByEmail(normalizedEmail)).thenReturn(true);

        var exception = assertThrows(AccountEmailAlreadyExistsException.class,
            () -> registerCustomerAccountUseCase.execute(command));

        assertEquals("Account email already exists: " + normalizedEmail,
            exception.getMessage());
        verify(accountRepository).findByIssuerAndSubject(issuer, subject);
        verify(customerRepository).existsByEmail(normalizedEmail);
        verify(accountRepository).existsByEmail(normalizedEmail);
        verify(accountRepository, never()).save(any(Account.class));
        verify(customerRepository, never()).save(any(Customer.class));
    }
}
