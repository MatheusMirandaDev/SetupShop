package br.com.setupshop.account.domain.repository;

import br.com.setupshop.account.domain.model.Account;

public interface AccountRepository {

    Account save(Account account);
}
