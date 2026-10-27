package br.com.setupshop.account.infrastructure.persistence;

import br.com.setupshop.account.domain.model.Account;
import br.com.setupshop.account.domain.model.AccountRole;
import br.com.setupshop.account.domain.repository.AccountRepository;
import br.com.setupshop.customer.domain.model.Customer;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Testcontainers
@Import(AccountRepositoryAdapter.class)
class AccountRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveCustomerAccount() {

        String email = "matheus.batista@gmail.com";
        Customer customer = new Customer(
            "Matheus Batista",
            email,
            "61999999999"
        );
        entityManager.persist(customer);

        String passwordHash = "placeholder-password-hash";
        Long customerId = customer.getId();
        AccountRole role = AccountRole.CUSTOMER;

        Account account = new Account(email, passwordHash, customerId, role);
        Account savedAccount = accountRepository.save(account);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(savedAccount.getId());
        Account accountFound = entityManager.find(Account.class, savedAccount.getId());

        assertNotNull(accountFound);
        assertEquals(email, accountFound.getEmail());
        assertEquals(passwordHash, accountFound.getPasswordHash());
        assertEquals(customerId, accountFound.getCustomerId());
        assertEquals(role, accountFound.getRole());
        assertNotNull(accountFound.getCreatedAt());
        assertNotNull(accountFound.getUpdatedAt());
    }

    @Test
    void shouldRejectDuplicateAccountEmail() {
        String email = "matheus.batista@gmail.com";
        Customer customer1 = new Customer(
            "Matheus Batista",
            email,
            "61999999999"
        );
        entityManager.persist(customer1);

        String passwordHash = "placeholder-password-hash";
        Long customerId1 = customer1.getId();
        AccountRole role = AccountRole.CUSTOMER;
        Account account1 = new Account(email, passwordHash, customerId1, role);
        Account savedAccount = accountRepository.save(account1);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(savedAccount.getId());

        Customer customer2 = new Customer(
            "Matheus Miranda",
            "matheus.miranda@gmail.com",
            "61888888888"
        );
        entityManager.persist(customer2);
        Long customerId2 = customer2.getId();
        Account account2 = new Account(email, passwordHash, customerId2, role);

        assertThrows(DataIntegrityViolationException.class,
            () -> {
                accountRepository.save(account2);
                entityManager.flush();
            }
        );
    }

    @Test
    void shouldRejectMultipleAccountsForSameCustomer() {
        String email1 = "matheus.batista@gmail.com";
        Customer customer = new Customer(
            "Matheus Batista",
            email1,
            "61999999999"
        );
        entityManager.persist(customer);

        String passwordHash = "placeholder-password-hash";
        Long customerId = customer.getId();
        AccountRole role = AccountRole.CUSTOMER;
        Account account1 = new Account(email1, passwordHash, customerId, role);
        Account savedAccount = accountRepository.save(account1);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(savedAccount.getId());

        String email2 = "matheus.miranda@gmail.com";

        Account account2 = new Account(email2, passwordHash, customerId, role);

        assertThrows(DataIntegrityViolationException.class,
            () -> {
                accountRepository.save(account2);
                entityManager.flush();
            }
        );
    }

    @Test
    void shouldRejectAccountWithNonexistentCustomer() {
        String email = "matheus.batista@gmail.com";
        String passwordHash = "placeholder-password-hash";
        AccountRole role = AccountRole.CUSTOMER;

        Account account = new Account(email, passwordHash, 99999L, role);

        assertThrows(DataIntegrityViolationException.class,
            () -> {
                accountRepository.save(account);
                entityManager.flush();
            }
        );
    }
}
