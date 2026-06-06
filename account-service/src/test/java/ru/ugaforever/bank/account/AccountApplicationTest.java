package ru.ugaforever.bank.account;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.ugaforever.bank.account.config.TestSecurityConfig;
import ru.ugaforever.bank.chassis.client.NotificationClient;

@SpringBootTest(properties = {
        "spring.cloud.openfeign.enabled=false"
})
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AccountApplicationTest {

    @MockitoBean
    private NotificationClient notificationClient;

    @Test
    void contextLoads() {
    }
}