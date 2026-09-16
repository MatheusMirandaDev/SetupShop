package br.com.setupshop.customer.application.usecase.list;

import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListCustomersUseCaseTest {

    private CustomerRepository customerRepository;
    private ListCustomersUseCase customersUseCase;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        customersUseCase = new ListCustomersUseCase(customerRepository);
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
        PageResult<Customer> pageResult = new PageResult<>(
            List.of(customer1, customer2), 0, 10, 2L, 1);

        when(customerRepository.findAll(pageQuery)).thenReturn(pageResult);

        var result = customersUseCase.execute(pageQuery);

        assertEquals(2, result.content().size());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(2L, result.totalElements());
        assertEquals(1, result.totalPages());
        assertSame(customer1, result.content().get(0));
        assertSame(customer2, result.content().get(1));

        verify(customerRepository).findAll(pageQuery);
    }
}
