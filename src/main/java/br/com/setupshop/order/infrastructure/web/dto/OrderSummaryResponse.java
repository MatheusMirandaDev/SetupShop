package br.com.setupshop.order.infrastructure.web.dto;

import br.com.setupshop.order.domain.model.OrderStatus;

import java.math.BigDecimal;

public record OrderSummaryResponse(
    Long id,

    Long customerId,

    OrderStatus status,

    BigDecimal total
) {
}
