package ru.ugaforever.bank.chassis.dto.cash;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequestDto {
    @NotNull(message = "ID аккаунта обязателен")
    private Long accountId;

    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть больше 0")
    private BigDecimal amount;
}
