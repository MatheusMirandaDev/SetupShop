package br.com.setupshop.customer.infrastructure.persistence;

import br.com.setupshop.customer.domain.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerRepositoryAdapterTest {

    private JpaCustomerRepository jpaCustomerRepository;
    private CustomerRepositoryAdapter customerRepositoryAdapter;

    @BeforeEach
    void setUp() {
        jpaCustomerRepository = mock(JpaCustomerRepository.class);
        customerRepositoryAdapter = new CustomerRepositoryAdapter(jpaCustomerRepository);
    }

    @Test
    void shouldSaveCustomer() {
        Customer customer = new Customer(
            "Matheus Miranda",
            "matheus.miranda@gmail.com",
            "61999999999"
        );

        when(jpaCustomerRepository.save(customer)).thenReturn(customer);
        Customer savedCustomer = customerRepositoryAdapter.save(customer);

        assertSame(customer, savedCustomer);
        verify(jpaCustomerRepository).save(customer);
    }

    @Test
    void shouldReturnTrueWhenCustomerEmailExists() {
        String customerEmail = "matheus.miranda@gmail.com";

        when(jpaCustomerRepository.existsByEmail(customerEmail)).thenReturn(true);
        boolean result = customerRepositoryAdapter.existsByEmail(customerEmail);

        assertTrue(result);
        verify(jpaCustomerRepository).existsByEmail(customerEmail);
    }

    @Test
    void shouldReturnCustomerWhenFoundById() {
        Long customerId = 1L;

        Customer customer = new Customer(
            "Matheus Miranda",
            "matheus.miranda@gmail.com",
            "61999999999"
        );

        when(jpaCustomerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        Optional<Customer> customerResult = customerRepositoryAdapter.findById(customerId);

        assertTrue(customerResult.isPresent());
        assertSame(customer, customerResult.orElseThrow());
        verify(jpaCustomerRepository).findById(customerId);
    }

    @Test
    void shouldReturnEmptyWhenCustomerIsNotFoundById() {
        Long customerId = 1L;

        when(jpaCustomerRepository.findById(customerId)).thenReturn(Optional.empty());
        Optional<Customer> customerResult = customerRepositoryAdapter.findById(customerId);

        assertTrue(customerResult.isEmpty());
        verify(jpaCustomerRepository).findById(customerId);
    }
}
