package br.com.setupshop.account.infrastructure.web.dto;

public record AuthenticatedIdentityResponse(
    String issuer,
    String subject
) {
}
