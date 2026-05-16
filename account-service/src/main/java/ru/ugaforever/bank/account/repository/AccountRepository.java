package ru.ugaforever.bank.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ugaforever.bank.account.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
