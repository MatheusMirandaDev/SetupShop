package br.com.setupshop.account.infrastructure.persistence;

import br.com.setupshop.account.domain.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaAccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIssuerAndSubject(String issuer, String subject);

    boolean existsByEmail(String email);
}
