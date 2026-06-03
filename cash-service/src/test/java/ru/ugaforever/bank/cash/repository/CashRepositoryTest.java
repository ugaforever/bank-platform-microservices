package ru.ugaforever.bank.cash.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.ugaforever.bank.cash.model.Cash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ugaforever.bank.chassis.dto.cash.CashAction;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class CashRepositoryTest {

    private static final Long CASH_ID = 1L;
    private static final String LOGIN = "ivanov";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100);

    @Autowired
    private CashRepository repository;

    @Test
    @DisplayName("save() — должен сохранить информацию о снятии / поплнении денег")
    void shouldSaveUser() {

        Cash saved = repository.save(newCash(LOGIN, CashAction.WITHDRAW, AMOUNT));

        assertThat(saved.getId()).isEqualTo(CASH_ID);
        assertThat(saved.getLogin()).isEqualTo(LOGIN);
        assertThat(saved.getAction()).isEqualTo(CashAction.WITHDRAW);
        assertThat(saved.getAmount()).isEqualTo(AMOUNT);
        assertThat(saved.getActionAt()).isNotNull();
        assertThat(saved.getActionAt()).isInstanceOf(Instant.class);
    }

    private Cash newCash(String login, CashAction action, BigDecimal amount) {
        return Cash.builder()
                .login(login)
                .action(action)
                .amount(amount)
                .actionAt(Instant.now())
                .build();
    }
}
