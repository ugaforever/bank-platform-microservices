package ru.ugaforever.bank.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationSource source;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant actionAt;

    @PrePersist
    protected void onCreate() {
        actionAt = Instant.now();
    }
}
