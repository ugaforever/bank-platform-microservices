package ru.ugaforever.bank.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.notification.mapper.NotificationMapper;
import ru.ugaforever.bank.notification.model.Notification;
import ru.ugaforever.bank.notification.repository.NotificationRepository;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationConsumer {

    private final NotificationRepository repository;

    @KafkaListener(topics = "bank.notification")
    public void listen(NotificationRequestDto request){
        log.info("Notification: source={}, message={}", request.getSource(), request.getMessage());

        Notification notification = Notification.builder()
                .source(request.getSource())
                .message(request.getMessage())
                .build();
        repository.save(notification);

        log.info("Notification completed: source={}, message={}", request.getSource(), request.getMessage());
    }
}
