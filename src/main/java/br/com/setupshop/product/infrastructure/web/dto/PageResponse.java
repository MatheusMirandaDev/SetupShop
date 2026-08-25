package br.com.setupshop.product.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated API response")
public record PageResponse<T>(

    @Schema(description = "Items returned on the current page")
    List<T> content,

    @Schema(
        description = "Zero-based current page number",
        example = "0"
    )
    int page,

    @Schema(
        description = "Maximum number of items per page",
        example = "20"
    )
    int size,

    @Schema(
        description = "Total number of available items",
        example = "42"
    )
    long totalElements,

    @Schema(
        description = "Total number of available pages",
        example = "3"
    )
    int totalPages) {
}
