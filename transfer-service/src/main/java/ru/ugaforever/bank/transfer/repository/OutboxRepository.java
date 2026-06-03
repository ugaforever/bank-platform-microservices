package ru.ugaforever.bank.transfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ugaforever.bank.transfer.model.TransferOutbox;

@Repository
public interface OutboxRepository extends JpaRepository<TransferOutbox, Long> {
}
