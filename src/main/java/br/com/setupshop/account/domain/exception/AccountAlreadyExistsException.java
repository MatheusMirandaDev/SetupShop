package br.com.setupshop.account.domain.exception;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException() {
        super("Account already exists for external identity");
    }
}
