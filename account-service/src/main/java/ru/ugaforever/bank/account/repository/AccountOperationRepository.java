package ru.ugaforever.bank.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ugaforever.bank.account.model.AccountOperation;

import java.util.Optional;

@Repository
public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {
    Optional<AccountOperation> findByIdempotencyKey(String idempotencyKey);
}
