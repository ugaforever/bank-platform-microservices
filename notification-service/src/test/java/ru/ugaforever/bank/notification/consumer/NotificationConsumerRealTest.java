package ru.ugaforever.bank.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;
import ru.ugaforever.bank.notification.model.Notification;
import ru.ugaforever.bank.notification.repository.DlqRepository;
import ru.ugaforever.bank.notification.repository.NotificationRepository;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedKafka(
        topics = {"bank.notification", "bank.notification.dlt"},
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DisplayName("Тесты с реальным бином KafkaTemplate")
class NotificationConsumerRealTest {

    private static final String NOTIFICATION_MESSAGE = "login=test, type=DEPOSIT, amount=100.00, newBalance=500.00";
    private static final String INVALID_JSON = "invalid json message";
    private static final String TOPIC = "bank.notification";
    private static final String DLT_TOPIC = "bank.notification.dlt";
    private static final String GROUP_ID = "notification-group";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DlqRepository dlqRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private NotificationConsumer notificationConsumer;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.consumer.group-id", () -> GROUP_ID);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.listener.ack-mode", () -> "manual");
    }

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        dlqRepository.deleteAll();
        reset(notificationConsumer);
    }

    @Test
    @DisplayName("Должен успешно обработать валидное сообщение")
    void shouldProcessValidMessage() throws Exception {

        // Arrange
        NotificationRequestDto request = NotificationRequestDto.builder()
                .source(NotificationSource.CASH_SERVICE)
                .message(NOTIFICATION_MESSAGE)
                .build();
        String messageJson = objectMapper.writeValueAsString(request);

        // Act
        kafkaTemplate.send(TOPIC, messageJson).get(5, TimeUnit.SECONDS);

        // Assert
        await()
                .pollDelay(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    assertThat(notificationRepository.count()).isEqualTo(1);

                    Notification saved = notificationRepository.findAll().get(0);
                    assertThat(saved.getSource()).isEqualTo(NotificationSource.CASH_SERVICE);
                    assertThat(saved.getMessage()).contains("test");
                    assertThat(saved.getMessage()).contains("DEPOSIT");
                });

        verify(notificationConsumer, times(1)).listen(anyString(), any());
    }

    @Test
    @DisplayName("Должен отправить невалидное сообщение в DLT топик Kafka")
    void shouldSendInvalidMessageToDltKafka() throws Exception {

        // Act
        kafkaTemplate.send(TOPIC, INVALID_JSON).get(5, TimeUnit.SECONDS);

        // Assert
        await()
                .pollDelay(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    assertThat(notificationRepository.count()).isEqualTo(0);
                    assertThat(dlqRepository.count()).isEqualTo(0);
                });

        verify(notificationConsumer, times(1)).listen(anyString(), any());
    }
}
