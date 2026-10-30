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

        Long customerId = customer.getId();
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        Account account = new Account(email, customerId, role, issuer, subject);
        Account savedAccount = accountRepository.save(account);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(savedAccount.getId());
        Account accountFound = entityManager.find(Account.class, savedAccount.getId());

        assertNotNull(accountFound);
        assertEquals(issuer, accountFound.getIssuer());
        assertEquals(subject, accountFound.getSubject());
        assertEquals(email, accountFound.getEmail());
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

        Long customerId1 = customer1.getId();
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject1 = "google-oauth2|123456789";
        Account account1 = new Account(email, customerId1, role, issuer, subject1);
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
        String subject2 = "google-oauth2|987654321";
        Account account2 = new Account(email, customerId2, role, issuer, subject2);

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

        Long customerId = customer.getId();
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject1 = "google-oauth2|123456789";
        Account account1 = new Account(email1, customerId, role, issuer, subject1);
        Account savedAccount = accountRepository.save(account1);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(savedAccount.getId());

        String subject2 = "google-oauth2|987654321";
        String email2 = "matheus.miranda@gmail.com";

        Account account2 = new Account(email2, customerId, role, issuer, subject2);

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
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        Account account = new Account(email, 99999L, role, issuer, subject);

        assertThrows(DataIntegrityViolationException.class,
            () -> {
                accountRepository.save(account);
                entityManager.flush();
            }
        );
    }

    @Test
    void shouldRejectDuplicateExternalIdentity() {
        String email1 = "matheus.batista@gmail.com";
        Customer customer1 = new Customer(
            "Matheus Batista",
            email1,
            "61999999999"
        );
        entityManager.persist(customer1);

        Long customerId = customer1.getId();
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";
        Account account1 = new Account(email1, customerId, role, issuer, subject);
        Account savedAccount = accountRepository.save(account1);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(savedAccount.getId());

        String email2 = "matheus.miranda@gmail.com";
        Customer customer2 = new Customer(
            "Matheus Miranda",
            email2,
            "61888888888"
        );
        entityManager.persist(customer2);
        Long customerId2 = customer2.getId();

        Account account2 = new Account(email2, customerId2, role, issuer, subject);

        assertThrows(DataIntegrityViolationException.class,
            () -> {
                accountRepository.save(account2);
                entityManager.flush();
            }
        );
    }

    @Test
    void shouldSaveDifferentAccountsFromSameIssuer() {
        String email1 = "matheus.batista@gmail.com";
        Customer customer1 = new Customer(
            "Matheus Batista",
            email1,
            "61999999999"
        );
        entityManager.persist(customer1);

        Long customerId1 = customer1.getId();
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject1 = "google-oauth2|123456789";
        Account account1 = new Account(email1, customerId1, role, issuer, subject1);
        Account savedAccount1 = accountRepository.save(account1);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(savedAccount1.getId());

        String email2 = "matheus.miranda@gmail.com";
        Customer customer2 = new Customer(
            "Matheus Miranda",
            email2,
            "61888888888"
        );
        entityManager.persist(customer2);
        Long customerId2 = customer2.getId();
        String subject2 = "google-oauth2|987654321";

        Account account2 = new Account(email2, customerId2, role, issuer, subject2);

        Account savedAccount2 = accountRepository.save(account2);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(savedAccount2.getId());

        Account accountFound1 = entityManager.find(Account.class, savedAccount1.getId());
        Account accountFound2 = entityManager.find(Account.class, savedAccount2.getId());

        assertNotNull(accountFound1);
        assertEquals(issuer, accountFound1.getIssuer());
        assertEquals(subject1, accountFound1.getSubject());
        assertEquals(email1, accountFound1.getEmail());
        assertEquals(customerId1, accountFound1.getCustomerId());
        assertEquals(role, accountFound1.getRole());
        assertNotNull(accountFound1.getCreatedAt());
        assertNotNull(accountFound1.getUpdatedAt());

        assertNotNull(accountFound2);
        assertEquals(issuer, accountFound2.getIssuer());
        assertEquals(subject2, accountFound2.getSubject());
        assertEquals(email2, accountFound2.getEmail());
        assertEquals(customerId2, accountFound2.getCustomerId());
        assertEquals(role, accountFound2.getRole());
        assertNotNull(accountFound2.getCreatedAt());
        assertNotNull(accountFound2.getUpdatedAt());
    }
}
