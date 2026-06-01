package ru.ugaforever.bank.chassis.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class    TransferRequestDto {

    @NotBlank(message = "Логин отправителя обязателен")
    private String fromLogin;

    @NotBlank(message = "Логин полусателя обязателен")
    private String toLogin;

    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть больше 0")
    private BigDecimal amount;
}
