package br.com.setupshop.customer.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
    @NotBlank
    @Size(max = 200)
    String name,

    @NotBlank
    @Email
    @Size(max = 255)
    String email,

    @NotBlank
    @Pattern(regexp = "[0-9]{11}")
    String phone
) {

}
