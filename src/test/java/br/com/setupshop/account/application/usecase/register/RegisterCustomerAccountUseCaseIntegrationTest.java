package br.com.setupshop.account.application.usecase.register;

import br.com.setupshop.account.infrastructure.persistence.AccountRepositoryAdapter;
import br.com.setupshop.customer.infrastructure.persistence.CustomerRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Testcontainers
@Import({RegisterCustomerAccountUseCase.class,
    CustomerRepositoryAdapter.class,
    AccountRepositoryAdapter.class})
class RegisterCustomerAccountUseCaseIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private RegisterCustomerAccountUseCase registerCustomerAccountUseCase;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldRollbackCustomerWhenAccountCreationFails() {

        String email = "matheus.miranda@gmail.com";
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "a".repeat(256);
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

        Long customerCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM customers WHERE email = ?",
            Long.class,
            email
        );
        assertEquals(0L, customerCount);

        var exception = assertThrows(IllegalArgumentException.class,
            () -> registerCustomerAccountUseCase.execute(command));

        assertEquals("Account subject cannot exceed 255 characters",
            exception.getMessage());

        Long customerAfterFailure = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM customers WHERE email = ?",
            Long.class,
            email
        );
        assertEquals(0L, customerAfterFailure);

        Long accountAfterFailure = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM accounts WHERE email = ?",
            Long.class,
            email
        );
        assertEquals(0L, accountAfterFailure);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldPersistCustomerAndAccountForExternalIdentity() {
        String email = "   MATHEUS.batista@gmail.com   ";
        String normalizedEmail = "matheus.batista@gmail.com";
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

        try {

            var result = registerCustomerAccountUseCase.execute(command);

            Long accountId = result.getId();
            Long customerId = result.getCustomerId();
            assertNotNull(accountId);
            assertNotNull(customerId);

            var persistedAccount = jdbcTemplate.queryForMap(
                """
                    SELECT email, issuer, subject, role, customer_id
                    FROM accounts
                    WHERE id = ?
                    """,
                accountId
            );
            assertEquals(normalizedEmail, persistedAccount.get("email"));
            assertEquals(issuer, persistedAccount.get("issuer"));
            assertEquals(subject, persistedAccount.get("subject"));
            assertEquals("CUSTOMER", persistedAccount.get("role"));
            assertEquals(result.getCustomerId(), persistedAccount.get("customer_id"));

            var persistedCustomer = jdbcTemplate.queryForMap(
                """
                    SELECT name, email, phone, active
                    FROM customers
                    WHERE id = ?
                    """,
                customerId
            );

            assertEquals(name, persistedCustomer.get("name"));
            assertEquals(normalizedEmail, persistedCustomer.get("email"));
            assertEquals(phone, persistedCustomer.get("phone"));
            assertEquals(true, persistedCustomer.get("active"));

            Long accountCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM accounts WHERE email = ?",
                Long.class,
                normalizedEmail
            );
            assertEquals(1L, accountCount);

            Long customerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customers WHERE email = ?",
                Long.class,
                normalizedEmail
            );
            assertEquals(1L, customerCount);
        } finally {
            jdbcTemplate.update(
                "DELETE FROM accounts WHERE email = ?", normalizedEmail
            );
            jdbcTemplate.update(
                "DELETE FROM customers WHERE email = ?", normalizedEmail
            );
        }
    }
}
