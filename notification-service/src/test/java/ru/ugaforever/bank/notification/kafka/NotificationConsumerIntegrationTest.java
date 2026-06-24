package ru.ugaforever.bank.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;
import ru.ugaforever.bank.notification.config.TestKafkaConfig;
import ru.ugaforever.bank.notification.model.Notification;
import ru.ugaforever.bank.notification.repository.NotificationRepository;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NotificationConsumerIntegrationTest {

        private static final String NOTIFICATION_MESSAGE = "Test notification message";

        @Autowired
        private KafkaTemplate<String, String> kafkaTemplate;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private NotificationRepository notificationRepository;

        @Test
        void shouldConsumeNotification() throws Exception {
                // Given
                NotificationRequestDto request = NotificationRequestDto.builder()
                        .source(NotificationSource.ACCOUNT_SERVICE)
                        .message(NOTIFICATION_MESSAGE)
                        .build();

                String jsonMessage = objectMapper.writeValueAsString(request);

                // When
                kafkaTemplate.send("bank.notification", NotificationSource.ACCOUNT_SERVICE.name(), jsonMessage);

                // Then
                await()
                        .atMost(60, TimeUnit.SECONDS)
                        .pollInterval(100, TimeUnit.MILLISECONDS)
                        .untilAsserted(() -> {
                               ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
                                verify(notificationRepository, times(1)).save(captor.capture());

                                Notification saved = captor.getValue();
                                assertThat(saved.getSource()).isEqualTo(NotificationSource.ACCOUNT_SERVICE);
                                assertThat(saved.getMessage()).isEqualTo("Test notification message");
                        });
        }
}
