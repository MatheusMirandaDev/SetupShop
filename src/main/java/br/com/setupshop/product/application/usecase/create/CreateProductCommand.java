package br.com.setupshop.product.application.usecase.create;

import java.math.BigDecimal;

public record CreateProductCommand(
        String name,
        String description,
        BigDecimal price
) { }