package br.com.setupshop.product.application.usecase.update;

import java.math.BigDecimal;

public record UpdateProductCommand(
        String name,
        String description,
        BigDecimal price
) {
}
