package br.com.setupshop.customer.infrastructure.persistence;

import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
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

import static org.junit.jupiter.api.Assertions.*;

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
}
