package ru.ugaforever.bank.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class TransferRequestDto {

    @NotNull(message = "ID аккаунта обязателен")
    private Long fromAccountId;

    @NotNull(message = "ID аккаунта обязателен")
    private Long toAccountId;

    @Positive(message = "Сумма должна быть больше 0")
    private BigDecimal amount;


}
