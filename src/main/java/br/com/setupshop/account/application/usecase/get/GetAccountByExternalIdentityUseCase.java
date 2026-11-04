package br.com.setupshop.account.application.usecase.get;

import br.com.setupshop.account.domain.exception.AccountNotFoundException;
import br.com.setupshop.account.domain.model.Account;
import br.com.setupshop.account.domain.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAccountByExternalIdentityUseCase {

    private final AccountRepository accountRepository;

    public GetAccountByExternalIdentityUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Account execute(String issuer, String subject) {
        return accountRepository
            .findByIssuerAndSubject(issuer, subject)
            .orElseThrow(AccountNotFoundException::new);
    }
}
