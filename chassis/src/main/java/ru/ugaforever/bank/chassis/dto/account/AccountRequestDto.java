package ru.ugaforever.bank.chassis.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountRequestDto {

    @NotBlank(message = "Логин не должен быть пустым")
    private String login;

    @NotBlank(message = "Имя не должно быть пустым")
    private String name;

    @NotBlank(message = "День рождения не должен быть пустым")
    private LocalDate birthdate;

    @Positive(message = "Сумма должна быть больше 0")
    private BigDecimal balance;

}
