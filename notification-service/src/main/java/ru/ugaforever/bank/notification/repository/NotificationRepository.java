package ru.ugaforever.bank.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ugaforever.bank.notification.model.Notification;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
