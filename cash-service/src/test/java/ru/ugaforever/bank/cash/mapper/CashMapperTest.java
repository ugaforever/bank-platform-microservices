package ru.ugaforever.bank.cash.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ugaforever.bank.cash.model.Cash;
import ru.ugaforever.bank.chassis.dto.cash.CashAction;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class CashMapperTest {

    private static final Long CASH_ID = 1L;
    private static final String LOGIN = "ivanov";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100);

    @Autowired
    private CashMapper mapper;

    @Test
    @DisplayName("Должен маппить Cash в CashResponseDto")
    void shouldMapToDto() {
        Cash cash = Cash.builder()
                .id(CASH_ID)
                .login(LOGIN)
                .action(CashAction.WITHDRAW)
                .amount(AMOUNT)
                .actionAt(Instant.now())
                .build();

        CashResponseDto dto = mapper.toDto(cash);

        assertThat(dto.getId()).isEqualTo(CASH_ID);
        assertThat(dto.getLogin()).isEqualTo(LOGIN);
        assertThat(dto.getAction()).isEqualTo(CashAction.WITHDRAW);
        assertThat(dto.getAmount()).isEqualTo(AMOUNT);
        assertThat(dto.getActionAt()).isNotNull();
        assertThat(dto.getActionAt()).isInstanceOf(Instant.class);

    }
}

