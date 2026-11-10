package br.com.setupshop.account.infrastructure.persistence;

import br.com.setupshop.account.domain.model.Account;
import br.com.setupshop.account.domain.repository.AccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AccountRepositoryAdapter implements AccountRepository {

    private final JpaAccountRepository accountRepository;

    public AccountRepositoryAdapter(JpaAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account save(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Optional<Account> findByIssuerAndSubject(String issuer, String subject) {
         return accountRepository.findByIssuerAndSubject(issuer, subject);
    }

    @Override
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }
}
