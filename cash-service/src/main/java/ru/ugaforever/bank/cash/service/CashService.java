package ru.ugaforever.bank.cash.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.cash.interceptor.IdempotencyInterceptor;
import ru.ugaforever.bank.cash.model.Cash;
import ru.ugaforever.bank.chassis.client.AccountClient;
import ru.ugaforever.bank.chassis.client.NotificationClient;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.CashAction;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.cash.mapper.CashMapper;
import ru.ugaforever.bank.cash.repository.CashRepository;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;
import ru.ugaforever.bank.chassis.exception.BusinessRuleException;
import ru.ugaforever.bank.chassis.exception.ValidationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CashService {

    private static final Logger log = LoggerFactory.getLogger(CashService.class);

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;
    private final CashRepository repository;
    private final CashMapper mapper;

    @CircuitBreaker(name = "cashServiceDeposit", fallbackMethod = "depositFallback")
    public CashResponseDto deposit(DepositRequestDto request) {
        log.info("Deposit cash: login={}, value={}", request.getLogin(), request.getAmount());

        String idempotencyKey = IdempotencyInterceptor.getCurrentIdempotencyKey();

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {

            Optional<Cash> existing = repository.findByIdempotencyKey(idempotencyKey);

            if (existing.isPresent()) {
                Cash cached = existing.get();
                log.info("Duplicate deposit request detected for key: {}", idempotencyKey);

                return CashResponseDto.builder()
                        .id(cached.getId())
                        .login(cached.getLogin())
                        .action(cached.getAction())
                        .amount(cached.getAmount())
                        .actionAt(cached.getActionAt())
                        .success(false)
                        .errorMessage("Duplicate deposit request")
                        .build();
            }
        }

        AccountResponseDto account = accountClient.deposit(request.getLogin(), request);
        log.debug("Balance updated: login={}, newBalance={}",
                account.getLogin(), account.getBalance().add(request.getAmount()));

        Cash.CashBuilder builder = Cash.builder()
                .login(request.getLogin())
                .amount(request.getAmount())
                .action(CashAction.DEPOSIT)
                .actionAt(Instant.now());
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            builder.idempotencyKey(idempotencyKey);
        }
        Cash cash = builder.build();

        // от простого к сложному: если idempotencyKey уже сохранен, значит deposit успешный (неуспешны не сохраняется)
        repository.save(cash);
        log.debug("Cash operation saved: id={}", cash.getId());

        NotificationRequestDto notificationRequestDto = NotificationRequestDto.builder()
                .source(NotificationSource.CASH_SERVICE)
                .message(String.format("login=%s, type=DEPOSIT, amount=%.2f, newBalance=%.2f",
                        request.getLogin(),
                        request.getAmount(),
                        account.getBalance().add(request.getAmount())))
                .build();
        notificationClient.sendNotification(notificationRequestDto);
        log.info("Notification sent: login={}, type=DEPOSIT", account.getLogin());

        log.info("Deposit completed: login={}, amount={}, newBalance={}",
                request.getLogin(), request.getAmount(), account.getBalance().add(request.getAmount()));

        return mapper.toDto(cash);
    }

    @CircuitBreaker(name = "cashServiceWithdraw", fallbackMethod = "withdrawFallback")
    public CashResponseDto withdraw(WithdrawRequestDto request) {
        log.info("Withdraw cash: login={}, amount={}", request.getLogin(), request.getAmount());

        String idempotencyKey = IdempotencyInterceptor.getCurrentIdempotencyKey();
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {

            Optional<Cash> existing = repository.findByIdempotencyKey(idempotencyKey);

            if (existing.isPresent()) {
                Cash cached = existing.get();
                log.info("Duplicate withdraw request detected for key: {}", idempotencyKey);

                return CashResponseDto.builder()
                        .id(cached.getId())
                        .login(cached.getLogin())
                        .action(cached.getAction())
                        .amount(cached.getAmount())
                        .actionAt(cached.getActionAt())
                        .success(false)
                        .errorMessage("Withdraw deposit request")
                        .build();
            }
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("The amount must be greater than 0");
        }

        AccountResponseDto account = accountClient.getAccount(request.getLogin());
        log.debug("Account found: login={}, currentBalance={}", account.getLogin(), account.getBalance());

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessRuleException("Insufficient funds");
        }

        accountClient.withdraw(request.getLogin(), request);
        log.debug("Balance updated: login={}, newBalance={}",
                account.getLogin(), account.getBalance().subtract(request.getAmount()));

        Cash.CashBuilder builder = Cash.builder()
                .login(request.getLogin())
                .amount(request.getAmount())
                .action(CashAction.WITHDRAW)
                .actionAt(Instant.now());
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            builder.idempotencyKey(idempotencyKey);
        }
        Cash cash = builder.build();
        repository.save(cash);
        log.debug("Cash operation saved: id={}", cash.getId());

        NotificationRequestDto notificationRequestDto = NotificationRequestDto.builder()
                .source(NotificationSource.CASH_SERVICE)
                .message(String.format("login=%s, type=WITHDRAWAL, amount=%.2f, newBalance=%.2f",
                        request.getLogin(),
                        request.getAmount(),
                        account.getBalance().subtract(request.getAmount())))
                .build();
        notificationClient.sendNotification(notificationRequestDto);
        log.info("Notification sent: login={}, type=WITHDRAWAL", account.getLogin());

        log.info("Withdraw completed: login={}, amount={}, newBalance={}",
                request.getLogin(), request.getAmount(), account.getBalance().subtract(request.getAmount()));

        return mapper.toDto(cash);
    }

    private CashResponseDto depositFallback(DepositRequestDto request, Exception e) {
        log.warn("Deposit circuit breaker opened");

        return CashResponseDto.builder()
                .id(null)
                .login(request.getLogin())
                .action(CashAction.DEPOSIT)
                .amount(request.getAmount())
                .actionAt(Instant.now())
                .success(false)
                .errorMessage("Deposit circuit breaker opened")
                .build();
    }

    private CashResponseDto withdrawFallback(WithdrawRequestDto request, Exception e) {
        log.warn("Withdraw circuit breaker opened");

        return CashResponseDto.builder()
                .id(null)
                .login(request.getLogin())
                .action(CashAction.WITHDRAW)
                .amount(request.getAmount())
                .actionAt(Instant.now())
                .success(false)
                .errorMessage("Withdraw circuit breaker opened")
                .build();
    }
}

