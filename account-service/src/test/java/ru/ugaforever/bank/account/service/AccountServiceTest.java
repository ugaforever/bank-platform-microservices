package ru.ugaforever.bank.account.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ugaforever.bank.account.dto.AccountResponseDto;
import ru.ugaforever.bank.account.mapper.AccountMapper;
import ru.ugaforever.bank.account.model.Account;
import ru.ugaforever.bank.account.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final String LOGIN = "ivanov";
    private static final String NAME = "Иванов Иван";
    private static final LocalDate BIRTHDATE = LocalDate.of(2001, 1, 1);
    private static final BigDecimal BALANCE = BigDecimal.valueOf(100);

    @Mock
    private AccountRepository repository;

    @Mock
    private AccountMapper mapper;

    @InjectMocks
    private AccountService service;

    @Test
    @DisplayName("getAccount(id) — должен вернуть пользователя, если он найден")
    void shouldReturnAccountById() {
        Account account = Account.builder()
                .id(ACCOUNT_ID)
                .login(LOGIN)
                .name(NAME)
                .birthdate(BIRTHDATE)
                .balance(BALANCE)
                .build();

        AccountResponseDto dto = AccountResponseDto.builder()
                .id(ACCOUNT_ID)
                .login(LOGIN)
                .name(NAME)
                .birthdate(BIRTHDATE)
                .balance(BALANCE)
                .build();

        when(repository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(mapper.toDto(account)).thenReturn(dto);

        AccountResponseDto result = service.getAccount(ACCOUNT_ID);

        assertThat(result.getId()).isEqualTo(ACCOUNT_ID);
        assertThat(result.getLogin()).isEqualTo(LOGIN);
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getBirthdate()).isEqualTo(BIRTHDATE);
        assertThat(result.getBalance()).isEqualTo(BALANCE);
    }

}
