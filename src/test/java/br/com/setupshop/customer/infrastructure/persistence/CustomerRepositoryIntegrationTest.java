package br.com.setupshop.customer.infrastructure.persistence;

import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import br.com.setupshop.shared.pagination.PageQuery;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@Import(CustomerRepositoryAdapter.class)
class CustomerRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveCustomer() {
        Customer customer = new Customer(
            "    Matheus Miranda   ",
            "   matheus.MIRANDA@gmail.com   ",
            "61999999999"
        );

        Customer savedCustomer = customerRepository.save(customer);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(savedCustomer.getId());

        Customer foundCustomer =
            entityManager.find(Customer.class, savedCustomer.getId());

        assertNotNull(foundCustomer);
        assertEquals(savedCustomer.getId(), foundCustomer.getId());
        assertEquals("Matheus Miranda", foundCustomer.getName());
        assertEquals("matheus.miranda@gmail.com", foundCustomer.getEmail());
        assertEquals("61999999999", foundCustomer.getPhone());
        assertTrue(foundCustomer.isActive());
        assertNotNull(foundCustomer.getCreatedAt());
        assertNotNull(foundCustomer.getUpdatedAt());
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        Customer customer = new Customer(
            "Matheus Miranda",
            "matheus.miranda@gmail.com",
            "61999999999"
        );

        customerRepository.save(customer);
        entityManager.flush();
        entityManager.clear();

        boolean emailExists = customerRepository.existsByEmail(customer.getEmail());

        assertTrue(emailExists);
    }

    @Test
    void shouldRejectDuplicateCustomerEmail() {
        Customer customer1 = new Customer(
            "Matheus",
            "matheus.miranda@gmail.com",
            "61888888888"
        );

        Customer customer2 = new Customer(
            "Miranda",
            "   MATHEUS.MIRANDA@GMAIL.COM   ",
            "61999999999"
        );

        customerRepository.save(customer1);
        entityManager.flush();
        entityManager.clear();

        assertThrows(DataIntegrityViolationException.class, () -> {
            customerRepository.save(customer2);
            entityManager.flush();
        });
    }

    @Test
    void shouldFindCustomerById() {
        Customer customer = new Customer(
            "Matheus",
            "matheus.miranda@gmail.com",
            "61888888888"
        );

        Customer savedCustomer = customerRepository.save(customer);
        entityManager.flush();
        entityManager.clear();

        Optional<Customer> customerResult = customerRepository.findById(savedCustomer.getId());
        assertTrue(customerResult.isPresent());

        Customer foundCustomer = customerResult.get();
        assertEquals(savedCustomer.getId(), foundCustomer.getId());
        assertEquals(savedCustomer.getName(), foundCustomer.getName());
        assertEquals(savedCustomer.getEmail(), foundCustomer.getEmail());
        assertEquals(savedCustomer.getPhone(), foundCustomer.getPhone());
    }

    @Test
    void shouldReturnPaginatedCustomersSortedById() {
        Customer customer1 = new Customer(
            "Matheus",
            "matheus.miranda@gmail.com",
            "61777777777"
        );

        Customer customer2 = new Customer(
            "Miranda",
            "miranda.matheus@gmail.com",
            "61888888888"
        );

        Customer customer3 = new Customer(
            "Batista",
            "batista.miranda@gmail.com",
            "61999999999"
        );

        Customer savedCustomer1 = customerRepository.save(customer1);
        Customer savedCustomer2 = customerRepository.save(customer2);
        Customer savedCustomer3 = customerRepository.save(customer3);
        entityManager.flush();
        entityManager.clear();

        PageQuery pageQuery = new PageQuery(0, 2);
        var result = customerRepository.findAll(pageQuery);

        assertEquals(2, result.content().size());
        assertEquals(0, result.page());
        assertEquals(2, result.size());
        assertEquals(3, result.totalElements());
        assertEquals(2, result.totalPages());
        assertEquals(savedCustomer1.getId(), result.content().get(0).getId());
        assertEquals(savedCustomer2.getId(), result.content().get(1).getId());
        assertFalse(
            result.content().stream()
                .anyMatch(customer -> customer.getId().equals(savedCustomer3.getId()))
        );
    }

    @Test
    void shouldPersistCustomerDeactivation() {
        Customer customer = new Customer(
            "Matheus",
            "matheus.miranda@gmail.com",
            "61888888888"
        );

        Customer savedCustomer = customerRepository.save(customer);
        savedCustomer.deactivate();
        customerRepository.save(savedCustomer);
        entityManager.flush();
        entityManager.clear();

        Customer reloadedCustomer = customerRepository.findById(savedCustomer.getId()).orElseThrow();;

        assertFalse(reloadedCustomer.isActive());
    }
}
