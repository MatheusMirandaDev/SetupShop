package br.com.setupshop.customer.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Data required to create a customer")
public record CreateCustomerRequest(
    @Schema(
        description = "Customer name",
        example = "Matheus Miranda"
    )
    @NotBlank
    @Size(max = 200)
    String name,

    @Schema(
        description = "Customer email",
        example = "matheus.miranda@gmail.com"
    )
    @NotBlank
    @Email
    @Size(max = 255)
    String email,

    @Schema(
        description = "Customer phone containing exactly 11 digits (0-9), without formatting.",
        example = "61999999999"
    )
    @NotBlank
    @Pattern(regexp = "[0-9]{11}")
    String phone
) {

}
