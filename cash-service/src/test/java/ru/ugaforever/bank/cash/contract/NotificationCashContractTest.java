package ru.ugaforever.bank.cash.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.ActiveProfiles;
import ru.ugaforever.bank.chassis.client.NotificationClient;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationResponseDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureStubRunner(
        ids = "ru.ugaforever.bank.notification:notification-service:+:stubs:9006",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("contract-test")
public class NotificationCashContractTest {

    private static final String MESSAGE = "Your operation was successful";

    @Autowired
    private NotificationClient notificationClient;

/*    @Test
    void shouldSendNotification() {

        NotificationRequestDto request = NotificationRequestDto.builder()
                .source(NotificationSource.CASH_SERVICE)
                .message(MESSAGE)
                .build();

        NotificationResponseDto result = notificationClient.sendNotification(request);

        assertThat(result.getSource()).isEqualTo(NotificationSource.CASH_SERVICE);
        assertThat(result.getMessage()).isEqualTo(MESSAGE);
        assertThat(result.getActionAt()).isNotNull();
        assertThat(result.getActionAt()).isInstanceOf(Instant.class);
    }*/
}
