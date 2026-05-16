package ru.ugaforever.bank.cash.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class DepositRequestDto {
    @NotNull(message = "ID аккаунта обязателен")
    private Long accountId;

    @Positive(message = "Сумма должна быть больше 0")
    private BigDecimal amount;
}
