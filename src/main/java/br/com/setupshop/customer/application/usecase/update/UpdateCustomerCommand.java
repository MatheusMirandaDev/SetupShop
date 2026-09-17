package br.com.setupshop.customer.application.usecase.update;

public record UpdateCustomerCommand(
    String name,
    String email,
    String phone
) {
}
