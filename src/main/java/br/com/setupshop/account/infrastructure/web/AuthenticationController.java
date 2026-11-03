package br.com.setupshop.account.infrastructure.web;

import br.com.setupshop.account.infrastructure.web.dto.AuthenticatedIdentityResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {




    @GetMapping("/me")
    ResponseEntity<AuthenticatedIdentityResponse> getAuth(@AuthenticationPrincipal Jwt jwt) {

        AuthenticatedIdentityResponse authenticatedIdentity =
            new AuthenticatedIdentityResponse(
                jwt.getIssuer().toString(),
                jwt.getSubject()
            );

        return ResponseEntity.ok(authenticatedIdentity);
    }
}
