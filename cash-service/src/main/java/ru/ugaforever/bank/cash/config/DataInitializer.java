package ru.ugaforever.bank.cash.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.ugaforever.bank.cash.model.Cash;
import ru.ugaforever.bank.cash.repository.CashRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CashRepository repository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (repository.count() == 0) {

            Cash cash1 = new Cash();
            cash1.setAccountId(1L);
            cash1.setBalance(BigDecimal.valueOf(100));

            Cash cash2 = new Cash();
            cash2.setAccountId(2L);
            cash2.setBalance(BigDecimal.valueOf(200));

            repository.save(cash1);
            repository.save(cash2);
        }
    }
}
