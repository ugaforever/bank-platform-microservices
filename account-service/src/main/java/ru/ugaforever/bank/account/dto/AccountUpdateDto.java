package ru.ugaforever.bank.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountUpdateDto {

    @Size(min = 2, max = 20, message = "Имя должно быть от 2 до 20 символов")
    private String name;

    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthdate;

    public boolean hasUpdates() {
        return name != null || birthdate != null;
    }

}
