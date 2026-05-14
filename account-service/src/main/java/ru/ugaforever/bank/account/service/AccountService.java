package ru.ugaforever.bank.account.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.ugaforever.bank.account.model.Account;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        accounts.put("ACC-001", new Account("ACC-001", "test-user", new BigDecimal("1000.00")));
        accounts.put("ACC-002", new Account("ACC-002", "other-user", new BigDecimal("500.00")));

        log.info("Инициализированы демо-аккаунты: {}", accounts.keySet());
    }

    public Account getAccount(String accountId) {
        log.debug("Запрошен аккаунт: {}", accountId);
        Account account = accounts.get(accountId);
        if (account == null) {
            log.warn("Аккаунт не найден: {}", accountId);
            throw new IllegalArgumentException("Аккаунт не найден: " + accountId);
        }
        return account;
    }
}
