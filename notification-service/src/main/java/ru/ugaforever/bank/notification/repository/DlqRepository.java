package ru.ugaforever.bank.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ugaforever.bank.notification.model.DeadLetterMessage;

@Repository
public interface DlqRepository extends JpaRepository<DeadLetterMessage, Long> {
}
