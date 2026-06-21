package ru.ugaforever.bank.chassis.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumerService {

    @KafkaListener(topics = "bank.notification")
    public void listen(String data) {
        log.info("Получены данные из топика: {}", data);
    }
}
