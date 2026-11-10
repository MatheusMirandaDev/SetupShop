package br.com.setupshop.account.domain.exception;

public class AccountEmailAlreadyExistsException extends RuntimeException {
    public AccountEmailAlreadyExistsException(String email) {
        super("Account email already exists: " + email);
    }
}
