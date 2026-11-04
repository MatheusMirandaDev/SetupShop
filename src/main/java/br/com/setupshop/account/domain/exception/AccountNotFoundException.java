package br.com.setupshop.account.domain.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException() {
        super("Account not found for external identity");
    }
}
