package br.com.setupshop.customer.application.usecase.get;

import br.com.setupshop.customer.domain.exception.CustomerNotFoundException;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetCustomerByIdUseCaseTest {

    private CustomerRepository customerRepository;
    private GetCustomerByIdUseCase customerUseCase;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        customerUseCase = new GetCustomerByIdUseCase(customerRepository);
    }

    @Test
    void shouldReturnCustomerWhenFound() {
        Long customerId = 1L;

        Customer customer = new Customer(
            "Matheus Miranda",
            "matheus.miranda@gmail.com",
            "61999999999"
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        var result = customerUseCase.execute(customerId);

        assertSame(customer, result);
        verify(customerRepository).findById(customerId);
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        Long customerId = 1L;

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());
        var exception = assertThrows(CustomerNotFoundException.class, () -> customerUseCase.execute(customerId));

        assertEquals("Customer not found with id: " + customerId, exception.getMessage());
        verify(customerRepository).findById(customerId);
    }
}
