package ru.ugaforever.bank.transfer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ugaforever.bank.transfer.metric.OutboxMetrics;
import ru.ugaforever.bank.transfer.model.OutboxStatus;
import ru.ugaforever.bank.transfer.model.TransferOutbox;
import ru.ugaforever.bank.transfer.repository.OutboxRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxPublisherService unit tests")
public class OutboxPublisherServiceTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private OutboxMetrics outboxMetrics;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OutboxPublisherService outboxPublisherService;

    private static final Long MESSAGE_ID = 1L;
    private static final Long TRANSFER_ID = 1L;
    private static final Integer MAX_RETRIES = 5;
    private static final String EVENT_TYPE = "DEPOSIT";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(outboxPublisherService, "batchSize", 100);
        ReflectionTestUtils.setField(outboxPublisherService, "maxRetries", MAX_RETRIES);
        ReflectionTestUtils.setField(outboxPublisherService, "publishTimeoutSeconds", 10L);
    }

    @Test
    @DisplayName("Должен обработать сообщения и обновить статус")
    void shouldProcessOutboxMessages() throws JsonProcessingException {
        // Arrange
        TransferOutbox message = createOutboxMessage(OutboxStatus.PENDING);

        SendResult<String, String> sendResult = createMockSendResult();
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        when(outboxRepository.save(any(TransferOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        boolean result = outboxPublisherService.processOutboxMessage(message);

        // Assert
        assertThat(result).isTrue();
        assertThat(message.getStatus()).isEqualTo(OutboxStatus.PROCESSED);

        verify(outboxRepository, times(1)).save(message);
        verify(outboxMetrics, times(1)).incrementProcessed();
        verify(outboxMetrics, never()).incrementFailed();

        verify(kafkaTemplate, times(1)).send(eq("bank.transfer"), messageCaptor.capture());
        String sentMessage = messageCaptor.getValue();

        JsonNode root = objectMapper.readTree(sentMessage);
        assertThat(root.has("after")).isTrue();

        JsonNode after = root.get("after");
        assertThat(after.has("transfer_id")).isTrue();
        assertThat(after.get("transfer_id").asLong()).isEqualTo(TRANSFER_ID);
        assertThat(after.has("event_type")).isTrue();
        assertThat(after.get("event_type").asText()).isEqualTo(EVENT_TYPE);
        assertThat(after.has("payload")).isTrue();
        assertThat(after.get("payload").asText()).isEqualTo(message.getPayload());
    }

    @Test
    @DisplayName("Должен обработать ошибку отправки в топик и обновить статус на FAILED")
    void shouldHandleKafkaErrorAndSetFailedStatus() {
        // Arrange
        TransferOutbox message = createOutboxMessage(OutboxStatus.PENDING);

        when(kafkaTemplate.send(anyString(), anyString())).thenThrow(new RuntimeException("Kafka connection error"));
        when(outboxRepository.save(any(TransferOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<TransferOutbox> outboxCaptor = ArgumentCaptor.forClass(TransferOutbox.class);

        // Act
        boolean result = outboxPublisherService.processOutboxMessage(message);

        // Assert
        assertThat(result).isFalse();

        verify(outboxRepository, times(1)).save(outboxCaptor.capture());
        TransferOutbox savedMessage = outboxCaptor.getValue();
        assertThat(savedMessage.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(savedMessage.getRetryCount()).isEqualTo(1);

        verify(outboxMetrics, times(1)).incrementFailed();
        verify(outboxMetrics, never()).incrementProcessed();

        verify(kafkaTemplate, times(1)).send(eq("bank.transfer"), anyString());
    }

    @Test
    @DisplayName("Должен установить статус EXHAUSTED при превышении лимита ретраев")
    void shouldSetExhaustedStatusWhenMaxRetriesExceeded() throws JsonProcessingException {
        // Arrange
        TransferOutbox message = createOutboxMessage(OutboxStatus.PENDING);
        message.setRetryCount(MAX_RETRIES - 1); // Предпоследняя попытка

        // Мокаем ошибку
        when(kafkaTemplate.send(anyString(), anyString())).thenThrow(new RuntimeException("Kafka connection error"));
        when(outboxRepository.save(any(TransferOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<TransferOutbox> outboxCaptor = ArgumentCaptor.forClass(TransferOutbox.class);

        // Act
        boolean result = outboxPublisherService.processOutboxMessage(message);

        // Assert
        assertThat(result).isFalse();

        verify(outboxRepository, times(1)).save(outboxCaptor.capture());
        TransferOutbox savedMessage = outboxCaptor.getValue();
        assertThat(savedMessage.getStatus()).isEqualTo(OutboxStatus.EXHAUSTED);
        assertThat(savedMessage.getRetryCount()).isEqualTo(MAX_RETRIES);

        verify(outboxMetrics, times(1)).incrementFailed();
    }

    @Test
    @DisplayName("Не должен ничего делать, если нет PENDING и FAILED сообщений")
    void shouldDoNothingWhenNoPendingAndFailedMessages() {
        // Arrange
        List<OutboxStatus> statuses = List.of(
                OutboxStatus.PENDING,
                OutboxStatus.FAILED
        );
        when(outboxRepository.findByStatusIn(statuses)).thenReturn(List.of());

        // Act
        outboxPublisherService.publishOutboxMessages();

        // Assert
        verify(outboxRepository, times(1)).findByStatusIn(statuses);
        verify(kafkaTemplate, never()).send(anyString(), anyString());
        verify(outboxRepository, never()).save(any(TransferOutbox.class));
        verify(outboxMetrics, never()).incrementProcessed();
        verify(outboxMetrics, never()).incrementFailed();
    }

    private TransferOutbox createOutboxMessage(OutboxStatus status) {
        return TransferOutbox.builder()
                .id(MESSAGE_ID)
                .transferId(TRANSFER_ID)
                .eventType(EVENT_TYPE)
                .payload("{\"type\":\"deposit\",\"toLogin\":\"account-2\",\"amount\":100.0}")
                .status(status)
                .retryCount(0)
                .build();
    }

    private SendResult<String, String> createMockSendResult() {

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("bank.transfer", 0),
                0L,
                0,
                0L,
                0,
                0);

        return new SendResult<>(null, metadata);
    }

}
