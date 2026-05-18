package ru.ugaforever.bank.cash.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;
import ru.ugaforever.bank.cash.mapper.CashMapper;
import ru.ugaforever.bank.cash.model.Cash;
import ru.ugaforever.bank.cash.repository.CashRepository;
import ru.ugaforever.bank.chassis.exception.AccountNotFoundException;
import ru.ugaforever.bank.chassis.exception.InsufficientFundsException;
import ru.ugaforever.bank.chassis.exception.ValidationException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class CashService {

    private static final Logger log = LoggerFactory.getLogger(CashService.class);

    private final CashRepository repository;
    private final CashMapper mapper;

    public CashResponseDto deposit(DepositRequestDto request) {
        log.info("Пополнение баланса: accountId={}, amount={}", request.getAccountId(), request.getAmount());

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Некорректная сумма пополнения: {}", request.getAmount());
            throw new ValidationException("Сумма пополнения должна быть больше 0");
        }

        Cash cash = repository.findById(request.getAccountId())
                .orElseThrow(() -> {
                    log.warn("Аккаунт не найден: {}", request.getAccountId());
                    return new AccountNotFoundException(request.getAccountId());
                });

        BigDecimal newBalance = cash.getBalance().add(request.getAmount());
        cash.setBalance(newBalance);

        Cash saved = repository.save(cash);
        log.info("Баланс пополнен: accountId={}, новый баланс={}", saved.getId(), saved.getBalance());

        // TODO notificationService.sendNotification(...);

        return mapper.toDto(saved);
    }

    public CashResponseDto withdraw(WithdrawRequestDto request) {
        log.info("Снятие денег: accountId={}, amount={}", request.getAccountId(), request.getAmount());

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Некорректная сумма снятия: {}", request.getAmount());
            throw new ValidationException("Сумма снятия должна быть больше 0");
        }

        Cash cash = repository.findById(request.getAccountId())
                .orElseThrow(() -> {
                    log.warn("Аккаунт не найден: {}", request.getAccountId());
                    return new AccountNotFoundException(request.getAccountId());
                });

        if (cash.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("Недостаточно средств: accountId={}, balance={}, requested={}",
                    cash.getAccountId(), cash.getBalance(), request.getAmount());

            throw new InsufficientFundsException(cash.getAccountId(), cash.getBalance(), request.getAmount());
        }

        BigDecimal newBalance = cash.getBalance().subtract(request.getAmount());
        cash.setBalance(newBalance);

        Cash saved = repository.save(cash);
        log.info("Снятие выполнено: accountId={}, новый баланс={}", saved.getId(), saved.getBalance());

        // TODO notificationService.sendNotification(...);

        return mapper.toDto(saved);
    }

    public BigDecimal getBalance(Long accountId) {
        log.debug("Проверка баланса: accountId={}", accountId);

        Cash cash = repository.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("Аккаунт не найден: {}", accountId);
                    return new AccountNotFoundException(accountId);
                });

        return cash.getBalance();
    }
}

