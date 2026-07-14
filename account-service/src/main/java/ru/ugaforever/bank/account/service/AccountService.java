package ru.ugaforever.bank.account.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.chassis.dto.account.AccountRequestDto;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;
import ru.ugaforever.bank.chassis.exception.AccountNotFoundException;
import ru.ugaforever.bank.account.mapper.AccountMapper;
import ru.ugaforever.bank.account.model.Account;
import ru.ugaforever.bank.account.model.AccountOperation;
import ru.ugaforever.bank.account.model.AccountOperationType;
import ru.ugaforever.bank.account.repository.AccountOperationRepository;
import ru.ugaforever.bank.account.repository.AccountRepository;
import ru.ugaforever.bank.chassis.exception.BusinessRuleException;
import ru.ugaforever.bank.chassis.exception.ValidationException;
import ru.ugaforever.bank.chassis.kafka.NotificationProducer;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final NotificationProducer notificationProducer;
    private final AccountRepository repository;
    private final AccountOperationRepository operationRepository;
    private final AccountMapper mapper;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        meterRegistry.counter("notification.account.create").increment(0);
    }

    public AccountResponseDto createAccount(AccountRequestDto dto) {
        Account account = mapper.toEntity(dto);
        Account saved = repository.save(account);

        NotificationRequestDto notificationRequestDto = NotificationRequestDto.builder()
                .source(NotificationSource.ACCOUNT_SERVICE)
                .message(String.format("Created new account: login=%s", saved.getLogin()))
                .build();
        notificationProducer.sendNotificationSync(notificationRequestDto);
        meterRegistry.counter("notification.account.create").increment();

        return mapper.toDto(saved);
    }

    public AccountResponseDto getAccount(String login) {

        log.debug("Get account: login={}", login);

        Timer timer = Timer.builder("account.get.time")
                .description("Time to get account by login")
                .tag("operation", "getAccount")
                .register(meterRegistry);

        return timer.record(() -> repository.findByLogin(login)
                .map(mapper::toDto)
                .orElseThrow(() -> {
                    log.warn("Account not found: {}", login);
                    return new AccountNotFoundException(login);
                }));
    }

    public List<AccountResponseDto> getAll() {
        log.debug("Get all accounts");

        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public AccountResponseDto updateAccount(String login, AccountUpdateDto updateDto) {

        if (updateDto == null) {
            throw new ValidationException("Account update data cannot be null");
        }

        log.info("Update account: login={}, fields={}", login, updateDto);

        if (!updateDto.hasUpdates()) {
            throw new ValidationException("The fields for updating are not specified");
        }

        Account account = repository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));
        log.debug("Account found: login={}", account.getLogin());

        if (updateDto.getName() != null) {
            account.setName(updateDto.getName());
        }

        if (updateDto.getBirthdate() != null) {
            account.setBirthdate(updateDto.getBirthdate());
        }

        Account saved = repository.save(account);
        log.info("Account updated: id={}", saved.getId());

        NotificationRequestDto notificationRequestDto = NotificationRequestDto.builder()
                .source(NotificationSource.ACCOUNT_SERVICE)
                .message(String.format("Account updated: login=%s", saved.getLogin()))
                .build();
        notificationProducer.sendNotificationSync(notificationRequestDto);
        log.info("Notification sent: login={}, type=UPDATE", account.getLogin());

        log.info("Update completed: login={}, fields={}", login, updateDto);

        return mapper.toDto(saved);
    }

    public AccountResponseDto deposit(
            String login,
            @NotNull(message = "Сумма обязательна") @Positive(message = "Сумма должна быть больше 0") BigDecimal amount) {

        return deposit(login, amount, null);
    }

    public AccountResponseDto deposit(
            String login,
            @NotNull(message = "Сумма обязательна") @Positive(message = "Сумма должна быть больше 0") BigDecimal amount,
            String idempotencyKey) {

        AccountResponseDto duplicate = getDuplicateOperationResponse(idempotencyKey);
        if (duplicate != null) {
            return duplicate;
        }

        Account account = repository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));

        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        Account saved = repository.save(account);
        saveOperation(idempotencyKey, login, AccountOperationType.DEPOSIT, amount, newBalance);
        log.info("Deposited {} to account {}, new balance: {}", amount, login, newBalance);

        return mapper.toDto(saved);
    }

    public AccountResponseDto withdraw(
            String login,
            @NotNull(message = "Сумма обязательна") @Positive(message = "Сумма должна быть больше 0") BigDecimal amount) {

        return withdraw(login, amount, null);
    }

    public AccountResponseDto withdraw(
            String login,
            @NotNull(message = "Сумма обязательна") @Positive(message = "Сумма должна быть больше 0") BigDecimal amount,
            String idempotencyKey) {

        AccountResponseDto duplicate = getDuplicateOperationResponse(idempotencyKey);
        if (duplicate != null) {
            return duplicate;
        }

        Account account = repository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));

        BigDecimal currentBalance = account.getBalance();
        if (currentBalance.compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient funds. Balance: " + currentBalance);
        }

        BigDecimal newBalance = currentBalance.subtract(amount);
        account.setBalance(newBalance);

        Account saved = repository.save(account);
        saveOperation(idempotencyKey, login, AccountOperationType.WITHDRAW, amount, newBalance);
        log.info("Withdrawn {} from account {}, new balance: {}", amount, login, newBalance);

        return mapper.toDto(saved);
    }

    private AccountResponseDto getDuplicateOperationResponse(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }

        return operationRepository.findByIdempotencyKey(idempotencyKey)
                .map(operation -> {
                    log.info("Duplicate account operation ignored: idempotencyKey={}, login={}, type={}",
                            idempotencyKey,
                            operation.getLogin(),
                            operation.getOperationType());

                    Account account = repository.findByLogin(operation.getLogin())
                            .orElseThrow(() -> new AccountNotFoundException(operation.getLogin()));
                    return mapper.toDto(account);
                })
                .orElse(null);
    }

    private void saveOperation(
            String idempotencyKey,
            String login,
            AccountOperationType operationType,
            BigDecimal amount,
            BigDecimal balanceAfter) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }

        operationRepository.save(AccountOperation.builder()
                .idempotencyKey(idempotencyKey)
                .login(login)
                .operationType(operationType)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .build());
    }
}
