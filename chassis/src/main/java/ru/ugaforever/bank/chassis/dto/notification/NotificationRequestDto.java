package ru.ugaforever.bank.chassis.dto.notification;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {

    @NotNull(message = "Источник уведомления обязателен")
    private NotificationSource source;

    @NotNull(message = "Текст уведомления обязателен")
    private String message;
}
