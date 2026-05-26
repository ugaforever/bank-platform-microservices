package ru.ugaforever.bank.transfer.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.chassis.client.AccountClient;
import ru.ugaforever.bank.chassis.client.NotificationClient;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;
import ru.ugaforever.bank.chassis.dto.transfer.TransferRequestDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferResponseDto;
import ru.ugaforever.bank.chassis.exception.BusinessRuleException;
import ru.ugaforever.bank.chassis.exception.ValidationException;
import ru.ugaforever.bank.transfer.mapper.TransferMapper;
import ru.ugaforever.bank.transfer.model.Transfer;
import ru.ugaforever.bank.transfer.repository.TransferRepository;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;
    private final TransferRepository repository;
    private final TransferMapper mapper;

    public String preview() {
        log.debug("Preview transfer request");
        return "Preview transfer request: ok.";
    }

    @CircuitBreaker(name = "transferServiceSubmit", fallbackMethod = "submitFallback")
    public TransferResponseDto submit(TransferRequestDto request/*, JwtAuthenticationToken authentication*/) {
        log.info("Transfer cash: from={}, from={}, amount={}", request.getFromLogin(), request.getToLogin(), request.getAmount());

        /*String username = authentication.getToken().getClaimAsString("preferred_username");
        if (request.getFromLogin() != username){
            throw new ValidationException("Пользователь " + username + " не является владельцем счёта " + request.getFromLogin());
        }*/

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("The amount must be greater than 0");
        }

        AccountResponseDto accountFrom = accountClient.getAccount(request.getFromLogin());
        log.debug("Account found: login={}, currentBalance={}", accountFrom.getLogin(), accountFrom.getBalance());

        if (accountFrom.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessRuleException("Insufficient funds");
        }

        WithdrawRequestDto withdrawDto = WithdrawRequestDto.builder()
                .login(request.getFromLogin())
                .amount(request.getAmount())
                .build();
        accountClient.withdraw(request.getFromLogin(), withdrawDto);

        DepositRequestDto depositDto = DepositRequestDto.builder()
                .login(request.getToLogin())
                .amount(request.getAmount())
                .build();
        accountClient.deposit(request.getToLogin(), depositDto);

        Transfer transfer = Transfer.builder()
                .fromLogin(request.getFromLogin())
                .toLogin(request.getToLogin())
                .amount(request.getAmount())
                .build();

        repository.save(transfer);
        log.debug("Transfer operation saved: id={}", transfer.getId());

        NotificationRequestDto notificationRequestDto = NotificationRequestDto.builder()
                .source(NotificationSource.TRANSFER_SERVICE)
                .message(String.format("from=%s, to=%s, type=TRANSFER, amount=%.2f",
                        transfer.getFromLogin(),
                        transfer.getToLogin(),
                        transfer.getAmount()))
                .build();
        notificationClient.sendNotification(notificationRequestDto);
        log.info("Notification sent: id={}", transfer.getId());

        log.info("Transfer completed: from={}, to={}, amount={}", request.getFromLogin(), request.getToLogin(), request.getAmount());
        return mapper.toDto(transfer);
    }

    private TransferResponseDto submitFallback(TransferRequestDto request, Exception e) {
        log.warn("Transfer circuit breaker opened");

        return TransferResponseDto.builder()
                .id(null)
                .fromLogin(request.getFromLogin())
                .toLogin(request.getToLogin())
                .amount(request.getAmount())
                .actionAt(Instant.now())
                .success(false)
                .errorMessage("Transfer circuit breaker opened")
                .build();
    }
}
