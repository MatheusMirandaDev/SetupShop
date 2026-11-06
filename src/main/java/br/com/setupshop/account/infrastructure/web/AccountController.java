package br.com.setupshop.account.infrastructure.web;

import br.com.setupshop.account.application.usecase.get.GetAccountByExternalIdentityUseCase;
import br.com.setupshop.account.infrastructure.web.dto.AccountResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final GetAccountByExternalIdentityUseCase getAccountByExternalIdentityUseCase;

    public AccountController(
        GetAccountByExternalIdentityUseCase getAccountByExternalIdentityUseCase) {
        this.getAccountByExternalIdentityUseCase = getAccountByExternalIdentityUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getCurrentAccount(
        @AuthenticationPrincipal Jwt jwt) {

        var accountResult =
            getAccountByExternalIdentityUseCase
                .execute(jwt.getIssuer().toString(), jwt.getSubject());

        AccountResponse accountResponse =
            new AccountResponse(
                accountResult.getId(),
                accountResult.getEmail(),
                accountResult.getRole(),
                accountResult.getCustomerId()
            );

        return ResponseEntity.ok(accountResponse);
    }
}
