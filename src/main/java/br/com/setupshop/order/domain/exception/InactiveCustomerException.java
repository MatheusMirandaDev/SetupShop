package br.com.setupshop.order.domain.exception;

public class InactiveCustomerException extends RuntimeException {
    public InactiveCustomerException(Long customerId) {
        super("Customer with id: " + customerId + " is inactive.");
    }
}
