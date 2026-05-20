package ru.ugaforever.bank.cash.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.cash.model.Cash;
import ru.ugaforever.bank.chassis.client.AccountClient;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.CashAction;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.cash.mapper.CashMapper;
import ru.ugaforever.bank.cash.repository.CashRepository;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;
import ru.ugaforever.bank.chassis.exception.BusinessRuleException;
import ru.ugaforever.bank.chassis.exception.ValidationException;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class CashService {

    private static final Logger log = LoggerFactory.getLogger(CashService.class);

    private final AccountClient accountClient;
    private final CashRepository repository;
    private final CashMapper mapper;

    public CashResponseDto deposit(DepositRequestDto request) {
        log.info("Deposit cash: login={}, value={}", request.getLogin(), request.getAmount());

        AccountResponseDto account = accountClient.deposit(request.getLogin(), request);

        Cash cash = Cash.builder()
                .accountId(account.getId())
                .action(CashAction.PUT)
                .amount(request.getAmount())
                .actionAt(Instant.now())
                .build();
        repository.save(cash);

        log.info("Deposit completed: login={}, value={}", request.getLogin(), request.getAmount());

        return mapper.toDto(cash);
    }

    public CashResponseDto withdraw(WithdrawRequestDto request) {
        log.info("Withdraw cash: login={}, amount={}", request.getLogin(), request.getAmount());

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Сумма должна быть больше 0");
        }

        AccountResponseDto account = accountClient.getAccount(request.getLogin());
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessRuleException("Недостаточно средств");
        }

        accountClient.withdraw(request.getLogin(), request);

        Cash cash = Cash.builder()
                .accountId(account.getId())
                .action(CashAction.GET)
                .amount(request.getAmount())
                .actionAt(Instant.now())
                .build();

        repository.save(cash);

        log.info("Withdraw completed: login={}, amount={}", request.getLogin(), request.getAmount());

        return mapper.toDto(cash);
    }
}

