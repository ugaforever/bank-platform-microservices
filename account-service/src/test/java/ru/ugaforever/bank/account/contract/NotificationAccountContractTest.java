package ru.ugaforever.bank.account.contract;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.ugaforever.bank.account.TestAccountApplication;
import ru.ugaforever.bank.account.repository.AccountRepository;
import ru.ugaforever.bank.chassis.client.NotificationClient;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationResponseDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = { TestAccountApplication.class },
        properties = {
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.loadbalancer.enabled=false",
                "spring.cloud.openfeign.enabled=false",
                "spring.security.enabled=false"
        }
)
@ActiveProfiles("contract-test")
public class NotificationAccountContractTest {

    private static final String MESSAGE = "Your operation was successful";

    @MockitoBean
    private NotificationClient notificationClient;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AccountRepository accountRepository;

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
