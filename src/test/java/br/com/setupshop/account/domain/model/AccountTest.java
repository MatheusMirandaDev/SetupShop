package br.com.setupshop.account.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {

    @Test
    void shouldCreateCustomerAccountLinkedToCustomer() {

        String email = "matheus.batista@gmail.com";
        String passwordHash = "placeholder-password-hash";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;

        Account account = new Account(email, passwordHash, customerId, role);

        assertEquals(email, account.getEmail());
        assertEquals(passwordHash, account.getPasswordHash());
        assertEquals(customerId, account.getCustomerId());
        assertEquals(role, account.getRole());
    }

    @Test
    void shouldNormalizeAccountEmail() {
        String email = "   matheus.MIRANDA@gmail.com   ";
        String passwordHash = "placeholder-password-hash";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;

        Account account = new Account(email, passwordHash, customerId, role);

        assertEquals("matheus.miranda@gmail.com", account.getEmail());
        assertEquals(passwordHash, account.getPasswordHash());
        assertEquals(customerId, account.getCustomerId());
        assertEquals(role, account.getRole());
    }

    @Test
    void shouldRejectNullAccountEmail() {
        String email = null;
        String passwordHash = "placeholder-password-hash";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, passwordHash, customerId, role));
    }

    @Test
    void shouldRejectBlankAccountEmail() {
        String email = "   ";
        String passwordHash = "placeholder-password-hash";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, passwordHash, customerId, role));
    }

    @Test
    void shouldRejectAccountEmailExceedingMaximumLength() {
        String email = "a".repeat(246) + "@email.com";
        String passwordHash = "placeholder-password-hash";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, passwordHash, customerId, role));
    }

    @Test
    void shouldAcceptAccountEmailAtMaximumLength() {
        String email = "a".repeat(245) + "@email.com";
        String passwordHash = "placeholder-password-hash";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;

        Account account = new Account(email, passwordHash, customerId, role);

        assertEquals(email, account.getEmail());
    }

    @Test
    void shouldRejectAccountEmailWithoutAtSign() {
        String email = "matheus.email.com";
        String passwordHash = "placeholder-password-hash";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, passwordHash, customerId, role));
    }

    @Test
    void shouldRejectAccountEmailWithWhitespace() {
        String email = "matheus miranda@email.com";
        String passwordHash = "placeholder-password-hash";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, passwordHash, customerId, role));
    }

    @Test
    void shouldRejectNullAccountPasswordHash() {
        String email = "matheus.batista@gmail.com";
        String passwordHash = null;
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, passwordHash, customerId, role));
    }

    @Test
    void shouldRejectBlankAccountPasswordHash() {
        String email = "matheus.batista@gmail.com";
        String passwordHash = "   ";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, passwordHash, customerId, role));
    }

    @Test
    void shouldRejectCustomerAccountWithoutCustomerId() {
        String email = "matheus.batista@gmail.com";
        String passwordHash = "placeholder-password-hash";
        Long customerId = null;
        AccountRole role = AccountRole.CUSTOMER;

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, passwordHash, customerId, role));
    }

    @Test
    void shouldRejectCustomerAccountWithZeroCustomerId() {
        String email = "matheus.batista@gmail.com";
        String passwordHash = "placeholder-password-hash";
        Long customerId = 0L;
        AccountRole role = AccountRole.CUSTOMER;

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, passwordHash, customerId, role));
    }

    @Test
    void shouldRejectCustomerAccountWithNegativeCustomerId() {
        String email = "matheus.batista@gmail.com";
        String passwordHash = "placeholder-password-hash";
        Long customerId = -1L;
        AccountRole role = AccountRole.CUSTOMER;

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, passwordHash, customerId, role));
    }

    @Test
    void shouldRejectNullAccountRole() {
        String email = "matheus.batista@gmail.com";
        String passwordHash = "placeholder-password-hash";
        Long customerId = 10L;
        AccountRole role = null;

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, passwordHash, customerId, role));
    }
}
