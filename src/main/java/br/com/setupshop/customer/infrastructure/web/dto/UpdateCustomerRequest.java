package br.com.setupshop.customer.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
    @Size(min = 1, max = 200)
    String name,

    @Email
    @Size(max = 255)
    String email,

    @Pattern(regexp = "[0-9]{11}")
    String phone
) {
}
