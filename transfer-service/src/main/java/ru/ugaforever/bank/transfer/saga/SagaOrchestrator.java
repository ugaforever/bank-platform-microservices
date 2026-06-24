package ru.ugaforever.bank.transfer.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.chassis.client.AccountClient;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;
import ru.ugaforever.bank.chassis.dto.transfer.TransferStatus;
import ru.ugaforever.bank.chassis.exception.ResourceNotFoundException;
import ru.ugaforever.bank.transfer.model.Transfer;
import ru.ugaforever.bank.transfer.model.TransferOutbox;
import ru.ugaforever.bank.transfer.producer.NotificationProducer;
import ru.ugaforever.bank.transfer.repository.OutboxRepository;
import ru.ugaforever.bank.transfer.repository.TransferRepository;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SagaOrchestrator {

    private final TransferRepository transferRepository;
    private final OutboxRepository outboxRepository;
    private final AccountClient accountClient;
    private final NotificationProducer notificationProducer;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "bank.transfer", groupId = "transfer-saga-orchestrator")
    public void handleOutboxEvent(String message, Acknowledgment ack) {
        log.info("Received outbox event: {}", message);

        try {
            JsonNode root = objectMapper.readTree(message);

            JsonNode after = root.get("after");
            if (after == null) {
                ack.acknowledge();
                log.error("No 'after' field in message");
                return;
            }

            Long transferId = after.get("transfer_id").asLong();
            String eventType = after.get("event_type").asText();

            String payloadString = after.get("payload").asText();
            JsonNode payload = objectMapper.readTree(payloadString);

            Transfer transfer = transferRepository.findById(transferId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transfer not found: " + transferId));

            switch (eventType) {
                case "WITHDRAW":
                    processWithdraw(transfer, payload);
                    break;
                case "DEPOSIT":
                    processDeposit(transfer, payload);
                    break;
                case "COMPENSATE":
                    processCompensate(transfer, payload);
                    break;
            }

            ack.acknowledge();

        } catch (ResourceNotFoundException | IllegalArgumentException | JsonProcessingException e) {
            // Permanent error. можно отправить в DLT (через бросание исключения ErrorHandler'у, в TODO)
            log.error("Permanent error: {}", e.getMessage(), e);
            ack.acknowledge();

        } catch (Exception e) {
            // Transient ошибка. не коммитим, Kafka перечитает (retry)
            log.warn("Transient error, will retry: {}", e.getMessage(), e);
            // ack НЕ вызываем!
        }
    }

    private void processWithdraw(Transfer transfer, JsonNode payload) {
        Long transferId = transfer.getId();
        String fromLogin = payload.get("fromLogin").asText();
        BigDecimal amount = new BigDecimal(payload.get("amount").asText());

        log.info("Processing WITHDRAW for transferId={}, fromLogin={}, amount={}",
                transferId, fromLogin, amount);

        if (transfer.getStatus() == TransferStatus.WITHDRAW_COMPLETED) {
            log.info("WITHDRAW already completed for transferId={}, skipping", transferId);
            return;
        }

        if (transfer.getStatus() == TransferStatus.WITHDRAW_PENDING) {
            log.warn("WITHDRAW already in progress for transferId={}, checking status...", transferId);
            return;
        }

        transfer.setStatus(TransferStatus.WITHDRAW_PENDING);
        transferRepository.save(transfer);

        try {
            WithdrawRequestDto withdrawDto = WithdrawRequestDto.builder()
                    .login(fromLogin)
                    .amount(amount)
                    .build();
            accountClient.withdraw(fromLogin, withdrawDto);

            transfer.setStatus(TransferStatus.WITHDRAW_COMPLETED);
            transfer.setSagaStep(1);
            transferRepository.save(transfer);
            log.info("Withdraw completed for transferId={}", transferId);

            publishNextEvent(transferId, "DEPOSIT", payload);

        } catch (Exception e) {
            //отмена перевода на этапе списания. нечего откатывать
            log.error("Withdraw failed for transferId={}", transferId, e);
            transfer.setStatus(TransferStatus.WITHDRAW_FAILED);
            transferRepository.save(transfer);
        }
    }

    private void processDeposit(Transfer transfer, JsonNode payload) {
        Long transferId = transfer.getId();
        String toLogin = payload.get("toLogin").asText();
        BigDecimal amount = new BigDecimal(payload.get("amount").asText());

        log.info("Processing DEPOSIT for transferId={}, toLogin={}, amount={}",
                transferId, toLogin, amount);

        if (transfer.getStatus() == TransferStatus.DEPOSIT_COMPLETED) {
            log.info("DEPOSIT already completed for transferId={}, skipping", transferId);
            return;
        }

        if (transfer.getStatus() == TransferStatus.DEPOSIT_PENDING) {
            log.warn("DEPOSIT already in progress for transferId={}, checking status...", transferId);
            return;
        }

        transfer.setStatus(TransferStatus.DEPOSIT_PENDING);
        transferRepository.save(transfer);

        try {
            DepositRequestDto depositDto = DepositRequestDto.builder()
                    .login(toLogin)
                    .amount(amount)
                    .build();
            accountClient.deposit(toLogin, depositDto);

            transfer.setStatus(TransferStatus.DEPOSIT_COMPLETED);
            transfer.setSagaStep(2);
            transferRepository.save(transfer);
            log.info("Deposit completed for transferId={}", transferId);

            publishCompleteEvent(transfer, payload);

        } catch (Exception e) {
            log.error("Deposit failed for transferId={}, starting compensation", transferId, e);
            transfer.setStatus(TransferStatus.DEPOSIT_FAILED);
            transferRepository.save(transfer);

            // Публикуем компенсацию
            publishCompensateEvent(transferId, payload);
        }
    }

    private void publishCompleteEvent(Transfer transfer, JsonNode payload) {
        Long transferId = transfer.getId();

        try {
            transfer.setStatus(TransferStatus.TRANSFER_COMPLETED);
            transfer.setSagaStep(3);
            transferRepository.save(transfer);
            log.info("Transfer COMPLETED successfully: transferId={}", transferId);

            sendNotification(transfer, payload);

        } catch (Exception e) {
            log.error("Failed to complete transfer: transferId={}", transferId, e);
        }
    }

    private void processCompensate(Transfer transfer, JsonNode payload) {
        Long transferId = transfer.getId();
        String fromLogin = payload.get("fromLogin").asText();
        BigDecimal amount = new BigDecimal(payload.get("amount").asText());

        log.warn("Processing COMPENSATION for transferId={}, returning money to={}, amount={}",
                transferId, fromLogin, amount);

        if (transfer.getStatus() == TransferStatus.TRANSFER_COMPENSATED) {
            log.info("TRANSFER already compensated for transferId={}, skipping", transferId);
            return;
        }

        try {
            // возвращаем деньги отправителю
            DepositRequestDto compensationDto = DepositRequestDto.builder()
                    .login(fromLogin)
                    .amount(amount)
                    .build();
            accountClient.deposit(fromLogin, compensationDto);

            transfer.setStatus(TransferStatus.TRANSFER_COMPENSATED);
            transfer.setSagaStep(0);
            transferRepository.save(transfer);
            log.info("Compensation completed for transferId={}", transferId);

            // отправляем уведомление об ошибке
            sendErrorNotification(transfer, payload);

        } catch (Exception e) {
            log.error("Compensation FAILED for transferId={}, NEED MANUAL INTERVENTION!", transferId, e);
            transfer.setStatus(TransferStatus.TRANSFER_FAILED);
            transferRepository.save(transfer);
            //сохранять в БД в таблицу ошибок компансаций transfer TODO:
        }
    }

    private void publishNextEvent(Long transferId, String eventType, JsonNode payload) {
        TransferOutbox outbox = TransferOutbox.builder()
                .transferId(transferId)
                .eventType(eventType)
                .payload(payload.toString())
                .build();
        outboxRepository.save(outbox);
        log.info("Published next event: transferId={}, eventType={}", transferId, eventType);
    }

    private void publishCompensateEvent(Long transferId, JsonNode payload) {
        TransferOutbox outbox = TransferOutbox.builder()
                .transferId(transferId)
                .eventType("COMPENSATE")
                .payload(payload.toString())
                .build();
        outboxRepository.save(outbox);
        log.warn("Compensation event published: transferId={}", transferId);
    }

    private void sendNotification(Transfer transfer, JsonNode payload) {
        try {
            NotificationRequestDto notification = NotificationRequestDto.builder()
                    .source(NotificationSource.TRANSFER_SERVICE)
                    .message(String.format("Transfer completed: from=%s, to=%s, amount=%.2f",
                            transfer.getFromLogin(), transfer.getToLogin(), transfer.getAmount()))
                    .build();
            notificationProducer.sendNotification(notification);
            log.info("Notification sent for transferId={}", transfer.getId());
        } catch (Exception e) {
            log.error("Failed to send notification for transferId={}", transfer.getId(), e);
            // считаем что ошибкит при отправке уведомлений не критичны, компенсацию не вызываем (пока)
        }
    }

    private void sendErrorNotification(Transfer transfer, JsonNode payload) {
        try {
            NotificationRequestDto notification = NotificationRequestDto.builder()
                    .source(NotificationSource.TRANSFER_SERVICE)
                    .message(String.format("Transfer FAILED: from=%s, to=%s, amount=%.2f. Compensated.",
                            transfer.getFromLogin(), transfer.getToLogin(), transfer.getAmount()))
                    .build();
            notificationProducer.sendNotification(notification);
        } catch (Exception e) {
            log.error("Failed to send error notification", e);
        }
    }
}
