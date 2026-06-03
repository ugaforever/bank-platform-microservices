package ru.ugaforever.bank.transfer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ugaforever.bank.chassis.dto.transfer.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fromLogin;
    private String toLogin;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    @Column(name = "saga_step")
    private Integer sagaStep;

    @Column(nullable = false, updatable = false)
    private Instant actionAt;

    @PrePersist
    protected void onCreate() {
        actionAt = Instant.now();
    }
}


