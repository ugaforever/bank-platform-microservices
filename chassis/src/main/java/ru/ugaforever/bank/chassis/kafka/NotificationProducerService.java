package ru.ugaforever.bank.chassis.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;

@Service
public class NotificationProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public NotificationProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(NotificationRequestDto request){

        Message<NotificationRequestDto> message = MessageBuilder
                .withPayload(request)
                .setHeader(KafkaHeaders.TOPIC, "bank.notification")
                .setHeader(KafkaHeaders.KEY, request.getSource().name())
                .build();

        kafkaTemplate.send(message);
    }
}