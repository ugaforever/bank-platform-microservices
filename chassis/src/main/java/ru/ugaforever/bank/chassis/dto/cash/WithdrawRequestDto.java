package ru.ugaforever.bank.chassis.dto.cash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WithdrawRequestDto {
    @NotNull(message = "ID аккаунта обязателен")
    private Long accountId;

    @Positive(message = "Сумма должна быть больше 0")
    private BigDecimal amount;
}
