package ru.ugaforever.bank.account.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.chassis.client.NotificationClient;
import ru.ugaforever.bank.chassis.dto.account.AccountRequestDto;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;
import ru.ugaforever.bank.chassis.exception.AccountNotFoundException;
import ru.ugaforever.bank.account.mapper.AccountMapper;
import ru.ugaforever.bank.account.model.Account;
import ru.ugaforever.bank.account.repository.AccountRepository;
import ru.ugaforever.bank.chassis.exception.BusinessRuleException;
import ru.ugaforever.bank.chassis.exception.ValidationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final NotificationClient notificationClient;
    private final AccountRepository repository;
    private final AccountMapper mapper;

    public AccountResponseDto createAccount(AccountRequestDto dto) {
        Account account = mapper.toEntity(dto);
        Account saved = repository.save(account);

        NotificationRequestDto notificationRequestDto = NotificationRequestDto.builder()
                .source(NotificationSource.ACCOUNT_SERVICE)
                .message(String.format("Created new account: login=%s", saved.getLogin()))
                .build();
        notificationClient.sendNotification(notificationRequestDto);

        return mapper.toDto(saved);
    }

    public AccountResponseDto getAccount(String login) {

        log.debug("Get account: login={}", login);

        return repository.findByLogin(login)
                .map(mapper::toDto)
                .orElseThrow(() -> {
                    log.warn("Account not found: {}", login);
                    return new AccountNotFoundException(login);
                });
    }

    public List<AccountResponseDto> getAll() {
        log.debug("Get all accounts");

        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public AccountResponseDto updateAccount(String login, AccountUpdateDto updateDto) {
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
        notificationClient.sendNotification(notificationRequestDto);
        log.info("Notification sent: login={}, type=UPDATE", account.getLogin());

        log.info("Update completed: login={}, fields={}", login, updateDto);

        return mapper.toDto(saved);
    }

    public AccountResponseDto deposit(
            String login,
            @NotNull(message = "Сумма обязательна") @Positive(message = "Сумма должна быть больше 0") BigDecimal amount) {

        Account account = repository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));

        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        Account saved = repository.save(account);
        log.info("Deposited {} to account {}, new balance: {}", amount, login, newBalance);

        return mapper.toDto(saved);
    }

    public AccountResponseDto withdraw(
            String login,
            @NotNull(message = "Сумма обязательна") @Positive(message = "Сумма должна быть больше 0") BigDecimal amount) {

        Account account = repository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));

        BigDecimal currentBalance = account.getBalance();
        if (currentBalance.compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient funds. Balance: " + currentBalance);
        }

        BigDecimal newBalance = currentBalance.subtract(amount);
        account.setBalance(newBalance);

        Account saved = repository.save(account);
        log.info("Withdrawn {} from account {}, new balance: {}", amount, login, newBalance);

        return mapper.toDto(saved);
    }
}
