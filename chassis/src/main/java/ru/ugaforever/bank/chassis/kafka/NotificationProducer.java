package ru.ugaforever.bank.chassis.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.exception.BankApplicationException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "bank.notification";

    public NotificationProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000)
    )
    public void sendNotificationSync(NotificationRequestDto request) {

        try {
            String jsonMessage = objectMapper.writeValueAsString(request);

            Message<String> message = MessageBuilder
                    .withPayload(jsonMessage)
                    .setHeader(KafkaHeaders.TOPIC, TOPIC)
                    .setHeader(KafkaHeaders.KEY, request.getSource().name())
                    .build();

            // блокируем и ждем
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(message);

            SendResult<String, String> result = future.get(5, TimeUnit.SECONDS);

            log.info("Notification sent: topic = {}, partition = {}, offset = {}, source={}, message={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    request.getSource(),
                    request.getMessage());

        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
            throw new BankApplicationException("Failed to send notification", e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Recover
    public void recover(NotificationRequestDto notification, Exception e) {
        log.error("All retries failed for: {}", notification, e);
        // здесь можно выполнить fallback с сохранением в БД или DLT
    }
}
