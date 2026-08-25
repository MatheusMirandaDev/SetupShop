package br.com.setupshop.product.infrastructure.web.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard API error response")
public record ApiErrorResponse(

    @Schema(
        description = "Date and time when the error occurred",
        example = "2026-08-25T12:00:00Z"
    )
    Instant timestamp,

    @Schema(
        description = "HTTP status code",
        example = "400"
    )
    int status,

    @Schema(
        description = "HTTP error reason",
        example = "Bad Request"
    )
    String error,

    @Schema(
        description = "Detailed error message",
        example = "Product price cannot be negative"
    )
    String message,

    @Schema(
        description = "Request path that produced the error",
        example = "/products/1"
    )
    String path
) {
}
