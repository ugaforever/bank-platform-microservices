package ru.ugaforever.bank.account.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ugaforever.bank.account.mapper.AccountMapper;
import ru.ugaforever.bank.account.model.Account;
import ru.ugaforever.bank.account.repository.AccountRepository;
import ru.ugaforever.bank.chassis.client.NotificationClient;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;
import ru.ugaforever.bank.chassis.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final String LOGIN = "ivanov";
    private static final String NAME = "Иванов Иван";
    private static final LocalDate BIRTHDATE = LocalDate.of(2001, 1, 1);
    private static final BigDecimal BALANCE = BigDecimal.valueOf(100);

    private static final String UPDATE_NAME = "Иванов Сергей";
    private static final LocalDate UPDATE_BIRTHDATE = LocalDate.of(2003, 3, 3);

    @Mock
    private AccountRepository repository;

    @Mock
    private AccountMapper mapper;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private AccountService service;

    @Test
    @DisplayName("getAccount(login) — должен вернуть пользователя, если он найден")
    void shouldReturnAccountByLogin() {
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

        when(repository.findByLogin(LOGIN)).thenReturn(Optional.of(account));
        when(mapper.toDto(account)).thenReturn(dto);

        AccountResponseDto result = service.getAccount(LOGIN);

        assertThat(result.getId()).isEqualTo(ACCOUNT_ID);
        assertThat(result.getLogin()).isEqualTo(LOGIN);
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getBirthdate()).isEqualTo(BIRTHDATE);
        assertThat(result.getBalance()).isEqualTo(BALANCE);
    }

    @Test
    @DisplayName("updateAccount(login, updateDto) — должен обновить и имя, и дату рождения одновременно")
    void shouldUpdateAccountNameAndBirthdateById() {

        AccountUpdateDto updateDto = AccountUpdateDto.builder()
                .name(UPDATE_NAME)
                .birthdate(UPDATE_BIRTHDATE)
                .build();

        Account existingAccount = Account.builder()
                .id(ACCOUNT_ID)
                .login(LOGIN)
                .name(NAME)
                .birthdate(BIRTHDATE)
                .balance(BALANCE)
                .build();

        Account updatedAccount = Account.builder()
                .id(ACCOUNT_ID)
                .login(LOGIN)
                .name(UPDATE_NAME)
                .birthdate(UPDATE_BIRTHDATE)
                .balance(BALANCE)
                .build();

        AccountResponseDto responseDto = AccountResponseDto.builder()
                .id(ACCOUNT_ID)
                .login(LOGIN)
                .name(UPDATE_NAME)
                .birthdate(UPDATE_BIRTHDATE)
                .balance(BALANCE)
                .build();

        when(repository.findByLogin(LOGIN)).thenReturn(Optional.of(existingAccount));
        when(repository.save(any(Account.class))).thenReturn(updatedAccount);
        when(mapper.toDto(updatedAccount)).thenReturn(responseDto);

        // when
        AccountResponseDto result = service.updateAccount(LOGIN, updateDto);

        // then
        assertThat(result.getId()).isEqualTo(ACCOUNT_ID);
        assertThat(result.getLogin()).isEqualTo(LOGIN);
        assertThat(result.getName()).isEqualTo(UPDATE_NAME);
        assertThat(result.getBirthdate()).isEqualTo(UPDATE_BIRTHDATE);
        assertThat(result.getBalance()).isEqualTo(BALANCE);

        verify(repository).findByLogin(LOGIN);
        verify(repository).save(any(Account.class));
        verify(mapper).toDto(updatedAccount);
    }

    @Test
    @DisplayName("updateAccount(login, updateDto) — должен выбросить исключение, если не указаны поля для обновления")
    void shouldThrowExceptionWhenNoFieldsToUpdate() {
        // given
        AccountUpdateDto updateDto = AccountUpdateDto.builder()
                .name(null)
                .birthdate(null)
                .build();

        // when & then
        assertThatThrownBy(() -> service.updateAccount(LOGIN, updateDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Не указаны поля для обновления");

        verify(repository, never()).findById(any());
        verify(repository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("updateAccount(id, updateDto) — должен выбросить исключение, если updateDto равен null")
    void shouldThrowExceptionWhenUpdateDtoIsNull() {
        // when & then
        assertThatThrownBy(() -> service.updateAccount(LOGIN, null))
                .isInstanceOf(NullPointerException.class);
    }

}
