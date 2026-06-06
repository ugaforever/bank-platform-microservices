package ru.ugaforever.bank.transfer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.ugaforever.bank.chassis.client.AccountClient;
import ru.ugaforever.bank.chassis.client.NotificationClient;
import ru.ugaforever.bank.transfer.config.TestSecurityConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class TransferApplicationTest {

    @MockitoBean
    private AccountClient accountClient;

    @MockitoBean
    private NotificationClient notificationClient;

    @Test
    void contextLoads() {
    }
}
