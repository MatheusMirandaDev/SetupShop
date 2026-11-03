package br.com.setupshop.account.infrastructure.web;

import br.com.setupshop.shared.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "AUTH0_ISSUER_URI=https://issuer.example/",
    "AUTH0_AUDIENCE=https://api.example"
})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {

        mockMvc
            .perform(get("/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidAccessToken() throws Exception {

        when(jwtDecoder.decode("invalid-token")).thenThrow(new BadJwtException("Invalid token"));

        mockMvc
            .perform(get("/auth/me").
                header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isUnauthorized());

        verify(jwtDecoder).decode("invalid-token");
    }

    @Test
    void shouldReturnAuthenticatedIdentity() throws Exception {

        Instant now = Instant.now();

        Jwt jwt = Jwt
            .withTokenValue("valid-token")
            .header("alg", "RS256")
            .issuer("https://setupshop-dev.us.auth0.com/")
            .subject("ficticio")
            .audience(List.of("https://api.setupshop.example"))
            .issuedAt(now)
            .expiresAt(now.plusSeconds(300))
            .build();

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);

        mockMvc
            .perform(get("/auth/me").
                header("Authorization", "Bearer valid-token"))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.issuer").value("https://setupshop-dev.us.auth0.com/"))
            .andExpect(jsonPath("$.subject").value("ficticio"));

        verify(jwtDecoder).decode("valid-token");
    }
}
