package ru.ugaforever.bank.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
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
    private final ObjectMapper objectMapper;

    private static final String DLT_TOPIC = "bank.notification.dlt";

    @KafkaListener(
            topics = "bank.notification",
            groupId = "notification-group"
    )
    public void listen(String messageJson, Acknowledgment ack) {

        try {
            NotificationRequestDto request = objectMapper.readValue(messageJson, NotificationRequestDto.class);
            log.info("Notification: source={}, message={}", request.getSource(), request.getMessage());

            Notification notification = Notification.builder()
                    .source(request.getSource())
                    .message(request.getMessage())
                    .build();
            repository.save(notification);

            ack.acknowledge();

            log.info("Notification completed: source={}, message={}", request.getSource(), request.getMessage());
        } catch (Exception e) {
            try {
                kafkaTemplate.send(DLT_TOPIC, messageJson);
                log.info("Message sent to DLT: {}", DLT_TOPIC);
            } catch (Exception ex) {
                log.error("Failed to send to DLT: {}", ex.getMessage(), ex);
            }

            ack.acknowledge();
        }
    }
}
