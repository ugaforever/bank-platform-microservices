package ru.ugaforever.bank.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.ugaforever.bank.notification.config.TestKafkaConfig;
import ru.ugaforever.bank.notification.model.DeadLetterMessage;
import ru.ugaforever.bank.notification.repository.DlqRepository;
import ru.ugaforever.bank.notification.repository.NotificationRepository;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestKafkaConfig.class)
@EmbeddedKafka(
        topics = {"bank.notification", "bank.notification.dlt"},
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
@DisplayName("Тесты с mock KafkaTemplate")
public class NotificationConsumerMockTest {

    private static final String NOTIFICATION_MESSAGE = "login=test, type=DEPOSIT, amount=100.00, newBalance=500.00";
    private static final String TOPIC = "bank.notification";
    private static final String DLT_TOPIC = "bank.notification.dlt";
    private static final String GROUP_ID = "notification-group";

    @Autowired
    @Qualifier("realKafkaTemplate")
    private KafkaTemplate<String, String> realKafkaTemplate;

    @Autowired
    @Qualifier("mockKafkaTemplate")
    private KafkaTemplate<String, String> mockKafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DlqRepository dlqRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private NotificationConsumer notificationConsumer;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        dlqRepository.deleteAll();
        reset(mockKafkaTemplate, notificationConsumer);

        doCallRealMethod().when(notificationConsumer).listen(anyString(), any());
    }

    @Test
    @DisplayName("Должен сохранить сообщение в БД при ошибке отправки в топик DLT")
    void shouldSaveToDlqDatabase() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Kafka broker unavailable"))
                .when(mockKafkaTemplate)
                .send(eq(DLT_TOPIC), anyString());

        // Act - Consumer получит сообщение и попытается отправить в DLT
        realKafkaTemplate.send(TOPIC, NOTIFICATION_MESSAGE).get(10, TimeUnit.SECONDS);

        Thread.sleep(5000);

        // Assert
        await()
                .pollDelay(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    assertThat(notificationRepository.count()).isEqualTo(0);
                    assertThat(dlqRepository.count()).isEqualTo(1);

                    DeadLetterMessage dlqMessage = dlqRepository.findAll().get(0);
                    assertThat(dlqMessage.getMessage()).isEqualTo(NOTIFICATION_MESSAGE);
                    assertThat(dlqMessage.getErrorMessage()).contains("Kafka broker unavailable");
                });

        verify(mockKafkaTemplate, times(1)).send(eq(DLT_TOPIC), anyString());
    }

    @Test
    @DisplayName(" Должен сохранить сообщение в БД при таймауте отправки в топик DLT")
    void shouldSaveToDlqDatabaseOnTimeout() throws Exception {
        // Arrange

        // Мокаем с таймаутом
        doAnswer(invocation -> {
            Thread.sleep(10000);
            return null;
        }).when(mockKafkaTemplate).send(anyString(), anyString());

        // Act
        realKafkaTemplate.send(TOPIC, NOTIFICATION_MESSAGE).get(5, TimeUnit.SECONDS);

        // Assert
        await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    assertThat(dlqRepository.count()).isEqualTo(1);

                    DeadLetterMessage dlqMessage = dlqRepository.findAll().get(0);
                    assertThat(dlqMessage.getMessage()).isEqualTo(NOTIFICATION_MESSAGE);
                    assertThat(dlqMessage.getErrorMessage()).isNotNull();
                });
    }
}
