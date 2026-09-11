package br.com.setupshop.customer.application.usecase.create;

import br.com.setupshop.customer.domain.exception.EmailAlreadyExistsException;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CreateCustomerUseCaseTest {

    @Test
    void shouldCreateAndSaveCustomerWhenEmailDoesNotExist() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CreateCustomerUseCase customerUseCase = new CreateCustomerUseCase(customerRepository);
        CreateCustomerCommand customerCommand = new CreateCustomerCommand(
            "Matheus Miranda",
            "   matheus.MIRANDA@gmail.com   ",
            "61999999999"
        );

        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(customerRepository.existsByEmail("matheus.miranda@gmail.com")).thenReturn(false);

        Customer customerResult = customerUseCase.execute(customerCommand);

        assertEquals("Matheus Miranda", customerResult.getName());
        assertEquals("matheus.miranda@gmail.com", customerResult.getEmail());
        assertEquals("61999999999", customerResult.getPhone());
        assertTrue(customerResult.isActive());
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(customerRepository).existsByEmail("matheus.miranda@gmail.com");
    }

    @Test
    void shouldNotSaveCustomerWhenEmailAlreadyExists() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CreateCustomerUseCase customerUseCase = new CreateCustomerUseCase(customerRepository);
        CreateCustomerCommand customerCommand = new CreateCustomerCommand(
            "Matheus Miranda",
            "   matheus.MIRANDA@gmail.com   ",
            "61999999999"
        );

        when(customerRepository.existsByEmail("matheus.miranda@gmail.com")).thenReturn(true);
        var exception = assertThrows(EmailAlreadyExistsException.class, () -> customerUseCase.execute(customerCommand));

        assertEquals("Customer email already exists: matheus.miranda@gmail.com", exception.getMessage());
        verify(customerRepository).existsByEmail("matheus.miranda@gmail.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldNotAccessRepositoryWhenCustomerDataIsInvalid() {
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        CreateCustomerUseCase customerUseCase = new CreateCustomerUseCase(customerRepository);
        CreateCustomerCommand customerCommand = new CreateCustomerCommand(
            "Matheus Miranda",
            "matheus. miranda@gmail.com",
            "61999999999"
        );

        assertThrows(IllegalArgumentException.class, () -> customerUseCase.execute(customerCommand));

        verifyNoInteractions(customerRepository);
    }
}
