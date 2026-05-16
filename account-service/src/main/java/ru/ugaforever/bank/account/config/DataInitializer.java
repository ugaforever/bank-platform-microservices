package ru.ugaforever.bank.account.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.ugaforever.bank.account.model.Account;
import ru.ugaforever.bank.account.repository.AccountRepository;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AccountRepository repository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (repository.count() == 0) {

            Account account1 = new Account();
            account1.setName("Иванов Иван");
            account1.setBirthdate(LocalDate.of(2001, 1, 1));

            Account account2 = new Account();
            account2.setName("Петров Петр");
            account2.setBirthdate(LocalDate.of(2002, 2, 2));

            repository.save(account1);
            repository.save(account2);
        }
    }
}
