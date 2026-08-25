package br.com.setupshop.product.infrastructure.web.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Validation error response containing errors by field")
public record ValidationErrorResponse(

    @Schema(
        description = "Date and time when the validation failed",
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
        description = "General validation error message",
        example = "Validation failed"
    )
    String message,

    @Schema(
        description = "Request path that produced the validation error",
        example = "/products"
    )
    String path,

    @Schema(
        description = "Validation messages organized by field name",
        example = "{\"name\": \"must not be blank\"}"
    )
    Map<String, String> fieldErrors
) {
}
