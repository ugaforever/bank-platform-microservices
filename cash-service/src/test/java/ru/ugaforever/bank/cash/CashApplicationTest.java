package ru.ugaforever.bank.cash;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.ugaforever.bank.cash.config.TestSecurityConfig;
import ru.ugaforever.bank.chassis.client.AccountClient;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class CashApplicationTest {

    @MockitoBean
    private AccountClient accountClient;

    @Test
    void contextLoads() {
    }
}