package br.com.setupshop.account.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {

    @Test
    void shouldCreateCustomerAccountLinkedToExternalIdentity() {
        String email = "matheus.batista@gmail.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        Account account = new Account(email, customerId, role, issuer, subject);

        assertEquals(email, account.getEmail());
        assertEquals(customerId, account.getCustomerId());
        assertEquals(role, account.getRole());
        assertEquals(issuer, account.getIssuer());
        assertEquals(subject, account.getSubject());
    }

    @Test
    void shouldNormalizeAccountEmail() {
        String email = "   matheus.MIRANDA@gmail.com   ";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        Account account = new Account(email, customerId, role, issuer, subject);

        assertEquals("matheus.miranda@gmail.com", account.getEmail());
        assertEquals(customerId, account.getCustomerId());
        assertEquals(role, account.getRole());
        assertEquals(issuer, account.getIssuer());
        assertEquals(subject, account.getSubject());
    }

    @Test
    void shouldRejectNullAccountEmail() {
        String email = null;
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldRejectBlankAccountEmail() {
        String email = "   ";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldRejectAccountEmailExceedingMaximumLength() {
        String email = "a".repeat(246) + "@email.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldAcceptAccountEmailAtMaximumLength() {
        String email = "a".repeat(245) + "@email.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        Account account = new Account(email, customerId, role, issuer, subject);

        assertEquals(email, account.getEmail());
    }

    @Test
    void shouldRejectAccountEmailWithoutAtSign() {
        String email = "matheus.email.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldRejectAccountEmailWithWhitespace() {
        String email = "matheus miranda@email.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldRejectCustomerAccountWithoutCustomerId() {
        String email = "matheus.batista@gmail.com";
        Long customerId = null;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldRejectCustomerAccountWithZeroCustomerId() {
        String email = "matheus.batista@gmail.com";
        Long customerId = 0L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldRejectCustomerAccountWithNegativeCustomerId() {
        String email = "matheus.batista@gmail.com";
        Long customerId = -1L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldRejectNullAccountRole() {
        String email = "matheus.batista@gmail.com";
        Long customerId = 10L;
        AccountRole role = null;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldRejectNullAccountIssuer() {
        String email = "matheus.batista@gmail.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = null;
        String subject = "google-oauth2|123456789";

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldRejectBlankAccountIssuer() {
        String email = "matheus.batista@gmail.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "   ";
        String subject = "google-oauth2|123456789";

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldRejectAccountIssuerExceedingMaximumLength() {
        String email = "matheus.batista@gmail.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://" + "a".repeat(248);
        String subject = "google-oauth2|123456789";

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldAcceptAccountIssuerAtMaximumLength() {
        String email = "matheus.batista@gmail.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://" + "a".repeat(247);
        String subject = "google-oauth2|123456789";

        Account account = new Account(email, customerId, role, issuer, subject);

        assertEquals(issuer, account.getIssuer());
    }

    @Test
    void shouldRejectNullAccountSubject() {
        String email = "matheus.batista@gmail.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = null;

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldRejectBlankAccountSubject() {
        String email = "matheus.batista@gmail.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "   ";

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldRejectAccountSubjectExceedingMaximumLength() {
        String email = "matheus.batista@gmail.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "a".repeat(256);

        assertThrows(IllegalArgumentException.class,
            () -> new Account(email, customerId, role, issuer, subject));
    }

    @Test
    void shouldAcceptAccountSubjectAtMaximumLength() {
        String email = "matheus.batista@gmail.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "a".repeat(255);

        Account account = new Account(email, customerId, role, issuer, subject);

        assertEquals(subject, account.getSubject());
    }
}
