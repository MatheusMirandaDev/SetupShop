package br.com.setupshop.customer.infrastructure.persistence;

import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerRepositoryAdapterTest {

    private JpaCustomerRepository jpaCustomerRepository;
    private CustomerRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaCustomerRepository = mock(JpaCustomerRepository.class);
        adapter = new CustomerRepositoryAdapter(jpaCustomerRepository);
    }

    @Test
    void shouldSaveCustomer() {
        Customer customer = new Customer(
            "Matheus Miranda",
            "matheus.miranda@gmail.com",
            "61999999999"
        );

        when(jpaCustomerRepository.save(customer)).thenReturn(customer);
        Customer savedCustomer = adapter.save(customer);

        assertSame(customer, savedCustomer);
        verify(jpaCustomerRepository).save(customer);
    }

    @Test
    void shouldReturnTrueWhenCustomerEmailExists() {
        String customerEmail = "matheus.miranda@gmail.com";

        when(jpaCustomerRepository.existsByEmail(customerEmail)).thenReturn(true);
        boolean result = adapter.existsByEmail(customerEmail);

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

        Optional<Customer> customerResult = adapter.findById(customerId);

        assertTrue(customerResult.isPresent());
        assertSame(customer, customerResult.orElseThrow());
        verify(jpaCustomerRepository).findById(customerId);
    }

    @Test
    void shouldReturnEmptyWhenCustomerIsNotFoundById() {
        Long customerId = 1L;

        when(jpaCustomerRepository.findById(customerId)).thenReturn(Optional.empty());
        Optional<Customer> customerResult = adapter.findById(customerId);

        assertTrue(customerResult.isEmpty());
        verify(jpaCustomerRepository).findById(customerId);
    }

    @Test
    void shouldReturnPaginatedCustomers() {
        Customer customer1 = new Customer(
            "Matheus",
            "matheus.miranda@gmail.com",
            "61888888888"
        );

        Customer customer2 = new Customer(
            "Miranda",
            "miranda.batista@gmail.com",
            "61999999999"
        );

        PageQuery pageQuery = new PageQuery(0, 10);
        PageRequest pageRequest =
            PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by("id").ascending());

        PageImpl<Customer> springPage = new PageImpl<>(List.of(customer1, customer2), pageRequest, 2);

        when(jpaCustomerRepository.findAll(pageRequest)).thenReturn(springPage);

        var result = adapter.findAll(pageQuery);

        assertEquals(2, result.content().size());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(2L, result.totalElements());
        assertEquals(1, result.totalPages());

        assertSame(customer1, result.content().get(0));
        assertSame(customer2, result.content().get(1));

        verify(jpaCustomerRepository).findAll(pageRequest);
    }
}
