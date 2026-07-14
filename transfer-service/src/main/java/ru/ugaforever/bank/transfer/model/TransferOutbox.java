package ru.ugaforever.bank.transfer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transfer_outbox")
public class TransferOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long transferId;

    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    private Integer retryCount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = OutboxStatus.PENDING;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        createdAt = Instant.now();
    }


}
