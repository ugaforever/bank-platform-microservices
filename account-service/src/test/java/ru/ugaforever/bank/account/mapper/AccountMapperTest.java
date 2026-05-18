package ru.ugaforever.bank.account.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.ugaforever.bank.account.model.Account;
import ru.ugaforever.bank.chassis.dto.account.AccountRequestDto;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;


public class AccountMapperTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final String LOGIN = "ivanov";
    private static final String NAME = "Иванов Иван";
    private static final LocalDate BIRTHDATE = LocalDate.of(2001, 1, 1);
    private static final BigDecimal BALANCE = BigDecimal.valueOf(100);

    private final AccountMapper mapper = new AccountMapperImpl();

    @Test
    @DisplayName("Должен маппить AccountRequestDto в Account без id")
    void shouldMapToEntity() {
        AccountRequestDto dto = new AccountRequestDto(LOGIN, NAME, BIRTHDATE, BALANCE);

        Account account = mapper.toEntity(dto);

        assertThat(account.getId()).isNull();
        assertThat(account.getLogin()).isEqualTo(LOGIN);
        assertThat(account.getName()).isEqualTo(NAME);
        assertThat(account.getBirthdate()).isEqualTo(BIRTHDATE);
        assertThat(account.getBalance()).isEqualTo(BALANCE);
    }

    @Test
    @DisplayName("Должен маппить Account в AccountResponseDto")
    void shouldMapToDto() {
        Account account = Account.builder()
                .id(ACCOUNT_ID)
                .login(LOGIN)
                .name(NAME)
                .birthdate(BIRTHDATE)
                .balance(BALANCE)
                .build();

        AccountResponseDto dto = mapper.toDto(account);

        assertThat(dto.getId()).isEqualTo(ACCOUNT_ID);
        assertThat(dto.getLogin()).isEqualTo(LOGIN);
        assertThat(dto.getName()).isEqualTo(NAME);
        assertThat(dto.getBirthdate()).isEqualTo(BIRTHDATE);
        assertThat(dto.getBalance()).isEqualTo(BALANCE);
    }
}
