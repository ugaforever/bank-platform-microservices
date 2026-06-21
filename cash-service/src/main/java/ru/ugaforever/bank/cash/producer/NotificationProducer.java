package ru.ugaforever.bank.cash.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.exception.BankApplicationException;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "bank.notification";

    public void sendNotification(NotificationRequestDto request){

        try {
            String jsonMessage = objectMapper.writeValueAsString(request);

            Message<String> message = MessageBuilder
                    .withPayload(jsonMessage)
                    .setHeader(KafkaHeaders.TOPIC, TOPIC)
                    .setHeader(KafkaHeaders.KEY, request.getSource().name())
                    .build();

            kafkaTemplate.send(message);
            log.info("Notification sent: source={}, message={}", request.getSource(), request.getMessage());
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
            throw new BankApplicationException("Failed to send notification", e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
