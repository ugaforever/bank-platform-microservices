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

    private static final String LOGIN = "ivanov";
    private static final String IDEMPOTENCY_KEY = "key";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100);

    @Autowired
    private CashRepository repository;

    @Test
    @DisplayName("save() — должен сохранить информацию о снятии / поплнении денег")
    void shouldSaveUser() {
        Cash сash = Cash.builder()
                .login(LOGIN)
                .idempotencyKey(IDEMPOTENCY_KEY)
                .action(CashAction.WITHDRAW)
                .amount(AMOUNT)
                .actionAt(Instant.now())
                .build();

        Cash saved = repository.save(сash);

        assertThat(saved.getLogin()).isEqualTo(LOGIN);
        assertThat(saved.getAction()).isEqualTo(CashAction.WITHDRAW);
        assertThat(saved.getAmount()).isEqualTo(AMOUNT);
        assertThat(saved.getActionAt()).isNotNull();
        assertThat(saved.getActionAt()).isInstanceOf(Instant.class);
    }
}
