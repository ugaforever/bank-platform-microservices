package ru.ugaforever.bank.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.notification.model.Notification;
import ru.ugaforever.bank.notification.repository.NotificationRepository;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationConsumer {

    private final NotificationRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(
            topics = "bank.notification",
            groupId = "notification-group",
            containerFactory = "kafkaContainerFactory"
    )
    public void listen(NotificationRequestDto request, Acknowledgment ack){
        log.info("Notification: source={}, message={}", request.getSource(), request.getMessage());

        try {
            Notification notification = Notification.builder()
                    .source(request.getSource())
                    .message(request.getMessage())
                    .build();
            repository.save(notification);
            ack.acknowledge();

            log.info("Notification completed: source={}, message={}", request.getSource(), request.getMessage());
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage(), e);

            // Отправляем в "Dead Letter Topic" для ручной обработки
            Message<NotificationRequestDto> message = MessageBuilder
                    .withPayload(request)
                    .setHeader(KafkaHeaders.TOPIC, "bank.notification.dlt")
                    .setHeader(KafkaHeaders.KEY, request.getSource().name())
                    .build();
            kafkaTemplate.send(message);

            ack.acknowledge();
        }
    }
}
