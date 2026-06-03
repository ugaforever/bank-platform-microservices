package ru.ugaforever.bank.transfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ugaforever.bank.transfer.model.Transfer;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
}