package br.com.setupshop.account.domain.repository;

import br.com.setupshop.account.domain.model.Account;

import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findByIssuerAndSubject(String issuer, String subject);

    boolean existsByEmail(String email);
}
