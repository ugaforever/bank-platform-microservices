package ru.ugaforever.bank.chassis.dto.cash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
@Builder
public class CashResponseDto {
    private Long id;
    private String login;
    private CashAction action;
    private BigDecimal amount;
    private Instant actionAt;

    private boolean success;
    private String errorMessage;
}
