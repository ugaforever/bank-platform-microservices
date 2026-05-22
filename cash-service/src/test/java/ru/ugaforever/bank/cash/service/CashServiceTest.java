package ru.ugaforever.bank.cash.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ugaforever.bank.cash.mapper.CashMapper;
import ru.ugaforever.bank.cash.model.Cash;
import ru.ugaforever.bank.cash.repository.CashRepository;
import ru.ugaforever.bank.chassis.client.AccountClient;
import ru.ugaforever.bank.chassis.client.NotificationClient;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.CashAction;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CashServiceTest {

    private static final Long CASH_ID = 1L;
    private static final String LOGIN = "ivanov";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100);

    @Mock
    private AccountClient accountClient;

    @Mock
    private CashRepository repository;

    @Mock
    private CashMapper mapper;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private CashService service;


    @Test
    @DisplayName("deposit — должен вернуть инофрмацию о депозите")
    void shouldReturnDeposit() {

        DepositRequestDto request = DepositRequestDto.builder()
                .login(LOGIN)
                .amount(AMOUNT)
                .build();

        AccountResponseDto accountResponse = AccountResponseDto.builder()
                .login(LOGIN)
                .balance(AMOUNT)
                .build();

        Cash cash = Cash.builder()
                .id(CASH_ID)
                .login(LOGIN)
                .action(CashAction.PUT)
                .amount(AMOUNT)
                .build();

        CashResponseDto expectedResponse = CashResponseDto.builder()
                .id(CASH_ID)
                .login(LOGIN)
                .action(CashAction.PUT)
                .amount(AMOUNT)
                .actionAt(Instant.now())
                .build();

        when(accountClient.deposit(eq(LOGIN), any(DepositRequestDto.class))).thenReturn(accountResponse);
        when(repository.save(any(Cash.class))).thenReturn(cash);
        when(mapper.toDto(any(Cash.class))).thenReturn(expectedResponse);

        // Act
        CashResponseDto result = service.deposit(request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.getId()).isEqualTo(CASH_ID);
        assertThat(result.getLogin()).isEqualTo(LOGIN);
        assertThat(result.getAmount()).isEqualTo(AMOUNT);
        assertThat(result.getActionAt()).isNotNull();
        assertThat(result.getActionAt()).isInstanceOf(Instant.class);

        verify(accountClient, times(1)).deposit(eq(LOGIN), any(DepositRequestDto.class));
    }
}
