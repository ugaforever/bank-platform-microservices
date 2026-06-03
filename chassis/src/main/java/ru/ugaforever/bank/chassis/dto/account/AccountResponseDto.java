package ru.ugaforever.bank.chassis.dto.account;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountResponseDto {
    private Long id;
    private String login;
    private String name;
    private LocalDate birthdate;
    private BigDecimal balance;
}
