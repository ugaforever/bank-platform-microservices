package ru.ugaforever.bank.cash.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ugaforever.bank.cash.model.Cash;


@Repository
public interface CashRepository extends JpaRepository<Cash, Long> {
}
