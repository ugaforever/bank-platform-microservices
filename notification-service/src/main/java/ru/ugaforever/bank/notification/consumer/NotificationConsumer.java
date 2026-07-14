package ru.ugaforever.bank.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.notification.model.DeadLetterMessage;
import ru.ugaforever.bank.notification.model.Notification;
import ru.ugaforever.bank.notification.repository.DlqRepository;
import ru.ugaforever.bank.notification.repository.NotificationRepository;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationConsumer {

    private final NotificationRepository repository;
    private final DlqRepository dlqRepository;
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
            log.error("Failed to process message: {}", e.getMessage(), e);
            // Синхронная отправка в DLT с ожиданием результата
            try {
                log.warn("Sending message to DLT: {}, error: {}", DLT_TOPIC, e.getMessage());

                SendResult<String, String> result = kafkaTemplate
                        .send(DLT_TOPIC, messageJson)
                        .get(5, TimeUnit.SECONDS);

                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Message sent to DLT: topic={}, partition={}, offset={}, timestamp={}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp());
            } catch (TimeoutException ex) {
                log.error("Timeout sending to DLT after 5 seconds: {}", ex.getMessage(), ex);
                saveToDeadLetterQueue(messageJson, ex);

            } catch (Exception ex) {
                // Критическая ошибка
                log.error("Failed to send to DLT: {}", ex.getMessage(), ex);
                saveToDeadLetterQueue(messageJson, ex);
            }

            ack.acknowledge();
            log.info("Message acknowledged after DLT processing");
        }
    }

    private void saveToDeadLetterQueue(String messageJson, Exception exception) {

        DeadLetterMessage dlqMessage = DeadLetterMessage.builder()
                .message(messageJson)
                .errorMessage(exception.getMessage())
                .build();

        dlqRepository.save(dlqMessage);

        log.warn("Message saved to DLQ database table as fallback");
    }
}
