package ru.ugaforever.bank.transfer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.transfer.metric.OutboxMetrics;
import ru.ugaforever.bank.transfer.model.OutboxStatus;
import ru.ugaforever.bank.transfer.model.TransferOutbox;
import ru.ugaforever.bank.transfer.repository.OutboxRepository;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherService {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxMetrics outboxMetrics;

    private static final String OUTBOX_TOPIC = "bank.transfer";
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRIES = 5;

    @Scheduled(
            fixedDelayString = "${outbox.publisher.fixed-delay:5000}",
            initialDelayString = "${outbox.publisher.initial-delay:10000}"
    )
    @Transactional
    public void publishOutboxMessages() {
        log.info("Starting outbox publishing job");

        try {
            List<OutboxStatus> statuses = Arrays.asList(
                    OutboxStatus.PENDING,
                    OutboxStatus.FAILED
            );
            List<TransferOutbox> messages = outboxRepository.findByStatusIn(statuses);

            if (messages.isEmpty()) {
                log.info("No outbox messages found");
                return;
            }
            log.info("Found {} outbox messages to process", messages.size());

            int processedCount = 0;
            int failedCount = 0;
            for (TransferOutbox message : messages) {
                boolean processed = processOutboxMessage(message);
                if (processed) {
                    processedCount++;
                } else {
                    failedCount++;
                }

                if (processedCount + failedCount >= BATCH_SIZE) {
                    log.info("Reached batch size limit, continuing next cycle");
                    break;
                }
            }

            log.info("Outbox publishing completed: processed={}, failed={}", processedCount, failedCount);

        } catch (Exception e) {
            log.error("Error in outbox publishing job: {}", e.getMessage(), e);
        }
    }

    @Transactional
    protected boolean processOutboxMessage(TransferOutbox message) {
        try {
            message.setStatus(OutboxStatus.PROCESSING);

            String kafkaMessage = buildKafkaMessage(message);

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(OUTBOX_TOPIC, kafkaMessage);
            SendResult<String, String> result = future.get(10, TimeUnit.SECONDS);

            log.info("Message sent to Kafka: transferId={}, eventType={}, offset={}",
                    message.getTransferId(),
                    message.getEventType(),
                    result.getRecordMetadata().offset());

            message.setStatus(OutboxStatus.PROCESSED);
            outboxRepository.save(message);
            outboxMetrics.incrementProcessed();

            return true;

        } catch (Exception e) {
            log.error("Failed to process outbox message: id={}, transferId={}, error={}",
                    message.getId(), message.getTransferId(), e.getMessage(), e);


            message.setStatus(OutboxStatus.FAILED);
            message.setRetryCount(message.getRetryCount() + 1);
            outboxRepository.save(message);
            outboxMetrics.incrementFailed();

            if (message.getRetryCount() >= MAX_RETRIES) {
                log.error("Message {} exceeded max retries {}", message.getId(), MAX_RETRIES);
                // уведомление админу: telegram, email, sms
            }

            return false;
        }
    }

    /**
     * Формирует сообщение в формате Debezium, который ожидает SagaOrchestrator
     * для совместивости в будущем
     *
     * Формат сообщения:
     * {
     *   "after": {
     *     "transfer_id": 1,
     *     "event_type": "DEPOSIT",
     *     "payload": "{\"type\":\"deposit\",\"toLogin\":\"account-2\",\"amount\":100.0}"
     *   }
     * }
     */
    private String buildKafkaMessage(TransferOutbox message) throws Exception {

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("transfer_id", message.getTransferId());
        after.put("event_type", message.getEventType());
        after.put("payload", message.getPayload());

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("after", after);

        return objectMapper.writeValueAsString(root);

    }
}
