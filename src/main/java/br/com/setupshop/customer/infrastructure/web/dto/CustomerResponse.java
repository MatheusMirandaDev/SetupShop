package br.com.setupshop.customer.infrastructure.web.dto;

public record CustomerResponse(
    Long id,
    String name,
    String email,
    String phone,
    boolean active
) {
}
