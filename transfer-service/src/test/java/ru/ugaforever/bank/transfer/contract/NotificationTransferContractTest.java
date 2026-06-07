package ru.ugaforever.bank.transfer.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.ugaforever.bank.chassis.client.AccountClient;
import ru.ugaforever.bank.chassis.client.NotificationClient;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationResponseDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"transfer.public.transfer_outbox"})
@AutoConfigureStubRunner(
        ids = "ru.ugaforever.bank.notification:notification-service:+:stubs:0",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("contract-test")
public class NotificationTransferContractTest {

    private static final String MESSAGE = "Your operation was successful";
    private static final String DATETIME = "2026-01-01T01:02:03.456Z";

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private AccountClient accountClient;

 /*   @Test
    void shouldSendNotification() {

        NotificationRequestDto request = NotificationRequestDto.builder()
                .source(NotificationSource.TRANSFER_SERVICE)
                .message(MESSAGE)
                .build();

        NotificationResponseDto result = notificationClient.sendNotification(request);

        assertThat(result.getSource()).isEqualTo(NotificationSource.TRANSFER_SERVICE);
        assertThat(result.getMessage()).isEqualTo(MESSAGE);
        assertThat(result.getActionAt()).isNotNull();
        assertThat(result.getActionAt()).isInstanceOf(Instant.class);
    }*/
}
