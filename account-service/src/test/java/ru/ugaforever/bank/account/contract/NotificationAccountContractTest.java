package ru.ugaforever.bank.account.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ugaforever.bank.account.config.TestContractConfig;
import ru.ugaforever.bank.chassis.client.NotificationClient;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationResponseDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestContractConfig.class)
@ActiveProfiles("contract-test")
public class NotificationAccountContractTest {

    private static final String MESSAGE = "Your operation was successful";

    @Autowired
    private NotificationClient notificationClient;

    @Test
    void shouldSendNotification() {

        NotificationResponseDto expectedResponse = NotificationResponseDto.builder()
                .source(NotificationSource.ACCOUNT_SERVICE)
                .message(MESSAGE)
                .actionAt(Instant.now())
                .build();

        when(notificationClient.sendNotification(any(NotificationRequestDto.class)))
                .thenReturn(expectedResponse);

        NotificationRequestDto request = NotificationRequestDto.builder()
                .source(NotificationSource.ACCOUNT_SERVICE)
                .message(MESSAGE)
                .build();

        NotificationResponseDto result = notificationClient.sendNotification(request);

        assertThat(result.getSource()).isEqualTo(NotificationSource.ACCOUNT_SERVICE);
        assertThat(result.getMessage()).isEqualTo(MESSAGE);
        assertThat(result.getActionAt()).isNotNull();
        assertThat(result.getActionAt()).isInstanceOf(Instant.class);
    }
}
