package ru.ugaforever.bank.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
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
