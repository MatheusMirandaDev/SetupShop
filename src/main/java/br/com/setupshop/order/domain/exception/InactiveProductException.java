package br.com.setupshop.order.domain.exception;

public class InactiveProductException extends RuntimeException {
    public InactiveProductException(Long productId) {
        super("Product with id: " + productId + " is inactive.");
    }
}
