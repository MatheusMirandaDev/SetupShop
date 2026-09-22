package br.com.setupshop.customer.application.usecase.deactivate;

import br.com.setupshop.customer.domain.exception.CustomerNotFoundException;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DeactivateCustomerUseCaseTest {

    private CustomerRepository customerRepository;
    private DeactivateCustomerUseCase deactivateCustomerUseCase;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        deactivateCustomerUseCase = new DeactivateCustomerUseCase(customerRepository);
    }

    @Test
    void shouldDeactivateAndSaveCustomer() {
        var customerId = 1L;
        String name = "name";
        String email = "email@gmail.com";
        String phone = "11111111111";

        Customer customer = new Customer(name, email, phone);

        assertTrue(customer.isActive());

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        deactivateCustomerUseCase.execute(customerId);

        assertFalse(customer.isActive());
        verify(customerRepository).findById(customerId);
        verify(customerRepository).save(customer);
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        var customerId = 1L;

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        var exception = assertThrows(CustomerNotFoundException.class,
            () -> deactivateCustomerUseCase.execute(customerId));

        assertEquals("Customer not found with id: " + customerId, exception.getMessage());
        verify(customerRepository).findById(customerId);
        verifyNoMoreInteractions(customerRepository);
    }
}
