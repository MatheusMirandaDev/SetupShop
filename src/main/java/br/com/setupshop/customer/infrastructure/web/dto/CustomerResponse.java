package br.com.setupshop.customer.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Customer data returned by the API")
public record CustomerResponse(
    @Schema(
        description = "Customer identifier",
        example = "1"
    )
    Long id,

    @Schema(
        description = "Customer name",
        example = "Matheus Miranda"
    )
    String name,

    @Schema(
        description = "Customer email",
        example = "matheus.miranda@gmail.com"
    )
    String email,

    @Schema(
        description = "Customer phone containing exactly 11 digits (0-9), without formatting.",
        example = "61999999999"
    )
    String phone,

    @Schema(
        description = "Indicates whether the customer is active",
        example = "true"
    )
    boolean active
) {

}
