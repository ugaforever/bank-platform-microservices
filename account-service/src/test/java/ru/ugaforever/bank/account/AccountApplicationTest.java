package ru.ugaforever.bank.account;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.ugaforever.bank.account.repository.AccountRepository;
import ru.ugaforever.bank.chassis.client.NotificationClient;

@SpringBootTest(properties = {
        "spring.cloud.openfeign.enabled=false"
})
@ActiveProfiles("test")
class AccountApplicationTest {

    @MockitoBean
    private NotificationClient notificationClient;

    @MockitoBean
    private AccountRepository accountRepository;

    @Test
    void contextLoads() {
    }
}