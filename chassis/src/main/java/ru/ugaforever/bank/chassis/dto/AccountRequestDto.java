package ru.ugaforever.bank.chassis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
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
