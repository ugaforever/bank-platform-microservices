package ru.ugaforever.bank.cash.model;

import jakarta.persistence.*;
import lombok.*;
import ru.ugaforever.bank.chassis.dto.cash.CashAction;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cashes")
public class Cash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String login;
    private CashAction action;
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private Instant actionAt;

    @Column(unique = true)
    private String idempotencyKey;

    @PrePersist
    protected void onCreate() {
        actionAt = Instant.now();
    }

    public static class CashBuilder {
        public CashBuilder idempotencyKey(String idempotencyKey) {
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                this.idempotencyKey = idempotencyKey;
            }
            return this;
        }
    }
}
