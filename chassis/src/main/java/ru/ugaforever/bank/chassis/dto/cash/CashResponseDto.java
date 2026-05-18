package ru.ugaforever.bank.chassis.dto.cash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class CashResponseDto {
    private Long id;
    private Long accountId;
    private BigDecimal balance;
}
