package ru.ugaforever.bank.cash.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
@Table(name = "cashes")
public class Cash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long accountId;
    private BigDecimal balance;
}
