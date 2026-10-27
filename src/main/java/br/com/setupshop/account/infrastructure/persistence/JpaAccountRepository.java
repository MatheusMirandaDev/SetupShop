package br.com.setupshop.account.infrastructure.persistence;

import br.com.setupshop.account.domain.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAccountRepository extends JpaRepository<Account, Long> {
}
