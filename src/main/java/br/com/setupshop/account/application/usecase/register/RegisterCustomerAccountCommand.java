package br.com.setupshop.account.application.usecase.register;

public record RegisterCustomerAccountCommand(
    String issuer,
    String subject,
    String email,
    String name,
    String phone
) {
}
