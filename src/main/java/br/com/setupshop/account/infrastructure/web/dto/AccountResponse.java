package br.com.setupshop.account.infrastructure.web.dto;

import br.com.setupshop.account.domain.model.AccountRole;

public record AccountResponse(
    Long id,
    String email,
    AccountRole role,
    Long customerId
) {
}
