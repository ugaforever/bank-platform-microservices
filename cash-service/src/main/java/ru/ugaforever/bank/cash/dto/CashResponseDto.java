package ru.ugaforever.bank.cash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class CashResponseDto {
    private Long id;
    private Long accountId;
    private BigDecimal balance;
}
