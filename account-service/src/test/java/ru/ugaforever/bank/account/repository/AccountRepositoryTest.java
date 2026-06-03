package ru.ugaforever.bank.account.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ugaforever.bank.account.model.Account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class AccountRepositoryTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final String LOGIN = "ivanov";
    private static final String NAME = "Иванов Иван";
    private static final LocalDate BIRTHDATE = LocalDate.of(2001, 1, 1);
    private static final BigDecimal BALANCE = BigDecimal.valueOf(100);

    @Autowired
    private AccountRepository repository;

    @Test
    @DisplayName("save() — должен сохранить пользователя")
    void shouldSaveUser() {
        Account saved = repository.save(newAccount(LOGIN, NAME, BIRTHDATE, BALANCE));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getLogin()).isEqualTo(LOGIN);
        assertThat(saved.getName()).isEqualTo(NAME);
        assertThat(saved.getBirthdate()).isEqualTo(BIRTHDATE);
        assertThat(saved.getBalance()).isEqualTo(BALANCE);
    }

    @Test
    @DisplayName("findById() — должен вернуть пользователя, если найден")
    void shouldFindUserById() {
        Account saved = repository.save(newAccount(LOGIN, NAME, BIRTHDATE, BALANCE));

        Optional<Account> result = repository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getLogin()).isEqualTo(LOGIN);
        assertThat(result.get().getName()).isEqualTo(NAME);
        assertThat(result.get().getBirthdate()).isEqualTo(BIRTHDATE);
        assertThat(result.get().getBalance()).isEqualTo(BALANCE);
    }

    @Test
    @DisplayName("shouldFindByLogin() — должен вернуть пользователя, если найден")
    void shouldFindByLogin() {
        Account saved = repository.save(newAccount(LOGIN, NAME, BIRTHDATE, BALANCE));

        Optional<Account> result = repository.findByLogin(LOGIN);

        assertThat(result).isPresent();
        assertThat(result.get().getLogin()).isEqualTo(LOGIN);
        assertThat(result.get().getName()).isEqualTo(NAME);
        assertThat(result.get().getBirthdate()).isEqualTo(BIRTHDATE);
        assertThat(result.get().getBalance()).isEqualTo(BALANCE);
    }

    @Test
    @DisplayName("shouldFindByName() — должен вернуть пользователя, если найден")
    void shouldFindByName() {
        Account saved = repository.save(newAccount(LOGIN, NAME, BIRTHDATE, BALANCE));

        Optional<Account> result = repository.findByName(NAME);

        assertThat(result).isPresent();
        assertThat(result.get().getLogin()).isEqualTo(LOGIN);
        assertThat(result.get().getName()).isEqualTo(NAME);
        assertThat(result.get().getBirthdate()).isEqualTo(BIRTHDATE);
        assertThat(result.get().getBalance()).isEqualTo(BALANCE);
    }

    @Test
    @DisplayName("findById() — должен вернуть пусто, если пользователя нет")
    void shouldReturnEmptyIfUserNotFound() {
        Optional<Account> result = repository.findById(999L);

        assertThat(result).isEmpty();
    }

    private Account newAccount(String login, String name, LocalDate birthdate, BigDecimal balance) {
        return Account.builder()
                .login(login)
                .name(name)
                .birthdate(birthdate)
                .balance(balance)
                .build();
    }

}
