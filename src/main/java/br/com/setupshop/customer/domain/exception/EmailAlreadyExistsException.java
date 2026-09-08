package br.com.setupshop.customer.domain.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Customer email already exists: " + email);
    }
}
