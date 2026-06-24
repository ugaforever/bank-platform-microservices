package ru.ugaforever.bank.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import ru.ugaforever.bank.notification.config.TestKafkaConfig;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EmbeddedKafka(
        topics = {"bank.notification", "bank.notification.dlt"},
        partitions = 1,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:0",
                "auto.create.topics.enable=true"
        }
)
@Import(TestKafkaConfig.class)
class NotificationApplicationTest {

    @Test
    void contextLoads() {

    }
}