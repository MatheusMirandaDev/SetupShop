package br.com.setupshop.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(
            auth -> auth.anyRequest().authenticated()
        );

        httpSecurity.oauth2ResourceServer(
            oauth2 -> oauth2.jwt(Customizer.withDefaults())
        );

        return httpSecurity.build();
    }
}
