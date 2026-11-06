package br.com.setupshop.account.infrastructure.web;

import br.com.setupshop.account.application.usecase.get.GetAccountByExternalIdentityUseCase;
import br.com.setupshop.account.domain.exception.AccountNotFoundException;
import br.com.setupshop.account.domain.model.Account;
import br.com.setupshop.account.domain.model.AccountRole;
import br.com.setupshop.shared.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "AUTH0_ISSUER_URI=https://issuer.example/",
    "AUTH0_AUDIENCE=https://api.example"
})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private GetAccountByExternalIdentityUseCase getAccountByExternalIdentityUseCase;

    @Test
    void shouldReturnCurrentAccountWhenExternalIdentityExists() throws Exception {

        String email = "matheus.batista@gmail.com";
        Long customerId = 10L;
        AccountRole role = AccountRole.CUSTOMER;
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";
        Account account = new Account(email, customerId, role, issuer, subject);
        ReflectionTestUtils.setField(account, "id", 1L);

        Instant now = Instant.now();
        Jwt jwt = Jwt
            .withTokenValue("valid-token")
            .header("alg", "RS256")
            .issuer(issuer)
            .subject(subject)
            .audience(List.of("https://api.setupshop.example"))
            .issuedAt(now)
            .expiresAt(now.plusSeconds(300))
            .build();

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);
        when(getAccountByExternalIdentityUseCase.execute(issuer, subject))
            .thenReturn(account);

        mockMvc
            .perform(get("/accounts/me").
                header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.customerId").value(customerId))
            .andExpect(jsonPath("$.role").value(role.name()));

        verify(getAccountByExternalIdentityUseCase).execute(issuer, subject);
    }

    @Test
    void shouldRejectUnauthenticatedCurrentAccountRequest() throws Exception {

        mockMvc
            .perform(get("/accounts/me"))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(getAccountByExternalIdentityUseCase);
    }

    @Test
    void shouldReturnNotFoundWhenAuthenticatedIdentityHasNoAccount() throws Exception{
        String issuer = "https://setupshop-test.auth0.com/";
        String subject = "google-oauth2|123456789";

        Instant now = Instant.now();
        Jwt jwt = Jwt
            .withTokenValue("valid-token")
            .header("alg", "RS256")
            .issuer(issuer)
            .subject(subject)
            .audience(List.of("https://api.setupshop.example"))
            .issuedAt(now)
            .expiresAt(now.plusSeconds(300))
            .build();

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);
        when(getAccountByExternalIdentityUseCase.execute(issuer, subject))
            .thenThrow(new AccountNotFoundException());

        mockMvc
            .perform(get("/accounts/me").
                header("Authorization", "Bearer valid-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Account not found for external identity"))
            .andExpect(jsonPath("$.path").value("/accounts/me"));

        verify(getAccountByExternalIdentityUseCase).execute(issuer, subject);
    }
}
