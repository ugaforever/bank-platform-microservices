package ru.ugaforever.bank.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class AccountResponseDto {
    private Long id;
    private String name;
    private LocalDate birthdate;
}
