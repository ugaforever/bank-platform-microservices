package ru.ugaforever.bank.account.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accounts",
        uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_login", columnNames = "login")
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Логин не должен быть пустым")
    private String login;

    private String name;
    private LocalDate birthdate;

    @Positive(message = "Сумма должна быть больше 0")
    private BigDecimal balance;
}
