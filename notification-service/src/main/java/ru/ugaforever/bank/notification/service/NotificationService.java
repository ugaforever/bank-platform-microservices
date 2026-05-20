package ru.ugaforever.bank.notification.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.chassis.client.AccountClient;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.CashAction;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationResponseDto;
import ru.ugaforever.bank.notification.mapper.NotificationMapper;
import ru.ugaforever.bank.notification.model.Notification;
import ru.ugaforever.bank.notification.repository.NotificationRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    public NotificationResponseDto notification(NotificationRequestDto request) {
        log.info("Notification: source={}, message={}", request.getSource(), request.getMessage());

        Notification notification = Notification.builder()
                .source(request.getSource())
                .message(request.getMessage())
                .build();
        repository.save(notification);

        log.info("Notification completed: source={}, message={}", request.getSource(), request.getMessage());

        return mapper.toDto(notification);

    }

}
