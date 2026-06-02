package ru.ugaforever.bank.cash.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.ugaforever.bank.cash.model.Cash;
import ru.ugaforever.bank.chassis.dto.cash.CashAction;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class CashMapperTest {

    private static final Long CASH_ID = 1L;
    private static final String LOGIN = "ivanov";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100);
    private static final String IDEMPOTENCY_KEY = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

    private CashMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CashMapper.class);
    }

    @Test
    @DisplayName("Должен маппить Cash в CashResponseDto")
    void shouldMapToDto() {
        Cash cash = Cash.builder()
                .id(CASH_ID)
                .login(LOGIN)
                .action(CashAction.WITHDRAW)
                .amount(AMOUNT)
                .idempotencyKey(IDEMPOTENCY_KEY)
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

