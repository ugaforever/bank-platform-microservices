package ru.ugaforever.bank.account;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.ugaforever.bank.account.repository.AccountRepository;

@SpringBootTest
@ActiveProfiles("test")
class AccountApplicationTest {

    @MockitoBean
    private AccountRepository accountRepository;

    @Test
    void contextLoads() {
    }
}