package br.com.setupshop.account.application.usecase.get;

import br.com.setupshop.account.domain.exception.AccountNotFoundException;
import br.com.setupshop.account.domain.model.Account;
import br.com.setupshop.account.domain.model.AccountRole;
import br.com.setupshop.account.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetAccountByExternalIdentityUseCaseTest {

    private AccountRepository accountRepository;
    private GetAccountByExternalIdentityUseCase accountUseCase;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        accountUseCase = new GetAccountByExternalIdentityUseCase(accountRepository);
    }

    @Test
    void shouldReturnAccountWhenExternalIdentityExists() {

        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        Account account = new Account(
            "matheus.miranda@gmail.com",
            2L,
            AccountRole.CUSTOMER,
            issuer,
            subject
        );

        when(accountRepository.findByIssuerAndSubject(issuer, subject))
            .thenReturn(Optional.of(account));

        var result = accountUseCase.execute(issuer, subject);

        assertSame(account, result);
        verify(accountRepository).findByIssuerAndSubject(issuer, subject);
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenExternalIdentityDoesNotExist() {
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        when(accountRepository.findByIssuerAndSubject(issuer, subject))
            .thenReturn(Optional.empty());

        var exception = assertThrows(AccountNotFoundException.class,
            () -> accountUseCase.execute(issuer, subject));

        assertEquals("Account not found for external identity", exception.getMessage());
        verify(accountRepository).findByIssuerAndSubject(issuer, subject);
    }
}
