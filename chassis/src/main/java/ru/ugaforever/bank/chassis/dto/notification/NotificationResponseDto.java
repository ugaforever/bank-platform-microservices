package ru.ugaforever.bank.chassis.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Builder
public class NotificationResponseDto {
    private Long id;
    private NotificationSource source;
    private String message;
    private Instant actionAt;
}
