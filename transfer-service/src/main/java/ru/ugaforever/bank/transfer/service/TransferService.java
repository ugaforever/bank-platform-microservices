package ru.ugaforever.bank.transfer.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.chassis.client.AccountClient;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferRequestDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferResponseDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferStatus;
import ru.ugaforever.bank.chassis.exception.BusinessRuleException;
import ru.ugaforever.bank.chassis.exception.ValidationException;
import ru.ugaforever.bank.transfer.mapper.TransferMapper;
import ru.ugaforever.bank.transfer.model.Transfer;
import ru.ugaforever.bank.transfer.model.TransferOutbox;
import ru.ugaforever.bank.transfer.repository.OutboxRepository;
import ru.ugaforever.bank.transfer.repository.TransferRepository;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountClient accountClient;
    private final TransferRepository transferRepository;
    private final OutboxRepository outboxRepository;
    private final TransferMapper mapper;

    @CircuitBreaker(name = "transferServiceSubmit", fallbackMethod = "submitFallback")
    public TransferResponseDto submit(TransferRequestDto request) {
        log.info("Transfer cash: from={}, to={}, amount={}", request.getFromLogin(), request.getToLogin(), request.getAmount());

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("The amount must be greater than 0");
        }

        if (request.getFromLogin().equals(request.getToLogin())){
            throw new BusinessRuleException("Cannot transfer to the same account");
        }

        AccountResponseDto accountFrom = accountClient.getAccount(request.getFromLogin());
        log.debug("Account found: login={}, currentBalance={}", accountFrom.getLogin(), accountFrom.getBalance());

        if (accountFrom.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessRuleException("Insufficient funds");
        }

        //saga step = 0
        Transfer transfer = transferRepository.save(
                Transfer.builder()
                        .fromLogin(request.getFromLogin())
                        .toLogin(request.getToLogin())
                        .amount(request.getAmount())
                        .status(TransferStatus.TRANSFER_PENDING)
                        .sagaStep(0)
                        .build()
        );
        log.debug("Transfer operation saved: id={}", transfer.getId());

        //outbox
        String payload = String.format(
                "{\"transferId\":%d,\"fromLogin\":\"%s\",\"toLogin\":\"%s\",\"amount\":%s}",
                transfer.getId(),
                request.getFromLogin(),
                request.getToLogin(),
                request.getAmount()
        );
        TransferOutbox outbox = TransferOutbox.builder()
                .transferId(transfer.getId())
                .eventType("WITHDRAW")
                .payload(payload)
                .build();
        outboxRepository.save(outbox);
        log.info("Outbox event saved: transferId={}, eventType=WITHDRAW", transfer.getId());

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
                .errorMessage(e.getMessage())
                .build();
    }
}
