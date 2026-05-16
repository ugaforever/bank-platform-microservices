package ru.ugaforever.bank.cash.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Data
public class WithdrawRequestDto {
    @NotNull(message = "ID аккаунта обязателен")
    private Long accountId;

    @Positive(message = "Сумма должна быть больше 0")
    private BigDecimal amount;
}
