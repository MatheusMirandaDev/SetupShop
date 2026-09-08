package br.com.setupshop.customer.application.usecase.create;

public record CreateCustomerCommand(
    String name,
    String email,
    String phone
) {
}
