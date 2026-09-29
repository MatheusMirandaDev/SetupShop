package br.com.setupshop.customer.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Fields used to partially update a customer. At least one field must be provided.")
public record UpdateCustomerRequest(

    @Schema(
        description = "New customer name. Omit to keep the current name.",
        example = "Matheus Miranda"
    )
    @Size(min = 1, max = 200)
    String name,

    @Schema(
        description = "New customer email. Omit to keep the current email.",
        example = "matheus.miranda@gmail.com"
    )
    @Email
    @Size(max = 255)
    String email,

    @Schema(
        description = "New customer phone containing exactly 11 digits (0-9), without formatting. Omit to keep the current phone.",
        example = "61999999999"
    )
    @Pattern(regexp = "[0-9]{11}")
    String phone
) {
}
