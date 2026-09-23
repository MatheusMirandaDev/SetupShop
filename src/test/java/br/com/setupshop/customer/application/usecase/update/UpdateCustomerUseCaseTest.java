package br.com.setupshop.customer.application.usecase.update;

import br.com.setupshop.customer.domain.exception.CustomerNotFoundException;
import br.com.setupshop.customer.domain.exception.EmailAlreadyExistsException;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class UpdateCustomerUseCaseTest {

    private CustomerRepository customerRepository;
    private UpdateCustomerUseCase updateCustomerUseCase;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        updateCustomerUseCase = new UpdateCustomerUseCase(customerRepository);
    }

    @Test
    void shouldUpdateAndSaveCustomer() {
        var customerId = 1L;
        String name = "name";
        String email = "email@gmail.com";
        String phone = "11111111111";

        Customer customer = new Customer(name, email, phone);

        String newName = "Matheus Miranda";
        String newEmail = "matheus.miranda@gmail.com";
        String newPhone = "61999999999";

        UpdateCustomerCommand updateCustomerCommand = new UpdateCustomerCommand(
            newName, newEmail, newPhone
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerRepository.existsByEmail(newEmail)).thenReturn(false);

        Customer resultCustomer = updateCustomerUseCase.execute(customerId, updateCustomerCommand);

        assertSame(customer, resultCustomer);
        assertEquals(newName, resultCustomer.getName());
        assertEquals(newEmail, resultCustomer.getEmail());
        assertEquals(newPhone, resultCustomer.getPhone());
        assertTrue(resultCustomer.isActive());
        verify(customerRepository).findById(customerId);
        verify(customerRepository).existsByEmail(newEmail);
        verify(customerRepository).save(customer);
    }

    @Test
    void shouldNotAccessRepositoryWhenNoFieldsAreProvided() {

        var customerId = 1L;

        UpdateCustomerCommand command =
            new UpdateCustomerCommand(null, null, null);

        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> updateCustomerUseCase.execute(customerId, command)
        );

        assertEquals("At least one field must be provided", exception.getMessage());
        verifyNoInteractions(customerRepository);
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        var customerId = 1L;

        String newName = "Matheus Miranda";
        String newEmail = "matheus.miranda@gmail.com";
        String newPhone = "61999999999";

        UpdateCustomerCommand updateCustomerCommand = new UpdateCustomerCommand(
            newName, newEmail, newPhone
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        var exception = assertThrows(
            CustomerNotFoundException.class,
            () -> updateCustomerUseCase.execute(customerId, updateCustomerCommand)
        );

        assertEquals("Customer not found with id: " + customerId, exception.getMessage());
        verify(customerRepository).findById(customerId);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void shouldNotSaveCustomerWhenNewEmailAlreadyExists() {
        var customerId = 1L;
        String name = "Matheus Miranda";
        String email = "matheus.miranda@gmail.com";
        String phone = "61999999999";
        Customer customer = new Customer(name, email, phone);

        String newEmail = "EMAIL.ALREADY.EXISTS@GMAIL.COM   ";
        String normalizedEmail = "email.already.exists@gmail.com";
        UpdateCustomerCommand updateCustomerCommand = new UpdateCustomerCommand(
            null, newEmail, null
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail(normalizedEmail)).thenReturn(true);

        var exception = assertThrows(
            EmailAlreadyExistsException.class,
            () -> updateCustomerUseCase.execute(customerId, updateCustomerCommand)
        );

        assertEquals(name, customer.getName());
        assertEquals(email, customer.getEmail());
        assertEquals(phone, customer.getPhone());
        assertEquals("Customer email already exists: " + normalizedEmail, exception.getMessage());
        verify(customerRepository).findById(customerId);
        verify(customerRepository).existsByEmail(normalizedEmail);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void shouldNotCheckEmailExistenceWhenEmailDoesNotChange() {
        var customerId = 1L;
        String name = "Matheus Miranda";
        String email = "matheus.miranda@gmail.com";
        String phone = "61999999999";
        Customer customer = new Customer(name, email, phone);

        String newName = "Roberto Batista";
        UpdateCustomerCommand updateCustomerCommand = new UpdateCustomerCommand(
            newName, null, null
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer resultCustomer = updateCustomerUseCase.execute(customerId, updateCustomerCommand);

        assertSame(customer, resultCustomer);
        assertEquals(newName, resultCustomer.getName());
        assertEquals(email, resultCustomer.getEmail());
        assertEquals(phone, resultCustomer.getPhone());
        assertTrue(resultCustomer.isActive());
        verify(customerRepository).findById(customerId);
        verify(customerRepository).save(customer);
        verifyNoMoreInteractions(customerRepository);
    }
}
