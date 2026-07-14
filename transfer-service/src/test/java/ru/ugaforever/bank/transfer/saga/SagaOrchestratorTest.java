package ru.ugaforever.bank.transfer.saga;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import ru.ugaforever.bank.chassis.client.AccountClient;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;
import ru.ugaforever.bank.chassis.dto.transfer.TransferStatus;
import ru.ugaforever.bank.chassis.kafka.NotificationProducer;
import ru.ugaforever.bank.transfer.model.Transfer;
import ru.ugaforever.bank.transfer.model.TransferOutbox;
import ru.ugaforever.bank.transfer.repository.OutboxRepository;
import ru.ugaforever.bank.transfer.repository.TransferRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SagaOrchestrator Unit тесты")
public class SagaOrchestratorTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private AccountClient accountClient;

    @Mock
    private NotificationProducer notificationProducer;

    @Mock
    private Acknowledgment ack;

    @InjectMocks
    private SagaOrchestrator sagaOrchestrator;

    @Spy
    private ObjectMapper objectMapper;

    private static final Long TRANSFER_ID = 1L;
    private static final String FROM_LOGIN = "account-1";
    private static final String TO_LOGIN = "account-2";
    private static final BigDecimal AMOUNT = new BigDecimal("100.00");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        sagaOrchestrator = new SagaOrchestrator(
                transferRepository,
                outboxRepository,
                accountClient,
                notificationProducer,
                objectMapper
        );
    }

    @Test
    @DisplayName("WITHDRAW: успешно списывает деньги и публикует DEPOSIT")
    void shouldProcessWithdrawSuccessfullyAndSaveToOutboxDeposit() throws Exception {
        // Arrange
        Transfer transfer = createTransfer(TransferStatus.TRANSFER_PENDING);
        JsonNode payload = createWithdrawPayload(FROM_LOGIN, AMOUNT);

        when(transferRepository.findById(TRANSFER_ID)).thenReturn(Optional.of(transfer));
        when(accountClient.withdraw(anyString(), any(WithdrawRequestDto.class)))
                .thenReturn(createAccountResponse(FROM_LOGIN, new BigDecimal("900.00")));

        List<Transfer> savedTransfers = new ArrayList<>();
        doAnswer(invocation -> {
            Transfer arg = invocation.getArgument(0);

            Transfer copy = Transfer.builder()
                    .id(arg.getId())
                    .fromLogin(arg.getFromLogin())
                    .toLogin(arg.getToLogin())
                    .amount(arg.getAmount())
                    .status(arg.getStatus())
                    .sagaStep(arg.getSagaStep())
                    .actionAt(arg.getActionAt())
                    .build();
            savedTransfers.add(copy);
            return arg;
        }).when(transferRepository).save(any(Transfer.class));

        ArgumentCaptor<TransferOutbox> outboxCaptor = ArgumentCaptor.forClass(TransferOutbox.class);

        String message = createOutboxMessage(TRANSFER_ID, "WITHDRAW", payload);

        // Act
        sagaOrchestrator.handleOutboxEvent(message, ack);

        // Assert
        assertThat(savedTransfers).hasSize(2);

        // Первый save - WITHDRAW_PENDING
        assertThat(savedTransfers.get(0).getStatus()).isEqualTo(TransferStatus.WITHDRAW_PENDING);
        assertThat(savedTransfers.get(0).getSagaStep()).isNull();

        // Второй save - WITHDRAW_COMPLETED
        assertThat(savedTransfers.get(1).getStatus()).isEqualTo(TransferStatus.WITHDRAW_COMPLETED);
        assertThat(savedTransfers.get(1).getSagaStep()).isEqualTo(1);

        // Проверяем создание Outbox для DEPOSIT
        verify(outboxRepository, times(1)).save(outboxCaptor.capture());
        TransferOutbox outbox = outboxCaptor.getValue();
        assertThat(outbox.getTransferId()).isEqualTo(TRANSFER_ID);
        assertThat(outbox.getEventType()).isEqualTo("DEPOSIT");
        assertWithdrawPayload(outbox, FROM_LOGIN, AMOUNT);

        verify(ack).acknowledge();
        verify(accountClient, times(1)).withdraw(anyString(), any(WithdrawRequestDto.class));
    }

    @Test
    @DisplayName("DEPOSIT: успешно зачисляет деньги и завершает перевод")
    void shouldProcessDepositSuccessfullyAndCompleteTransfer() throws Exception {
        // Arrange
        Transfer transfer = createTransfer(TransferStatus.WITHDRAW_COMPLETED);
        JsonNode payload = createDepositPayload(TO_LOGIN, AMOUNT);

        when(transferRepository.findById(TRANSFER_ID)).thenReturn(Optional.of(transfer));
        when(accountClient.deposit(anyString(), any(DepositRequestDto.class)))
                .thenReturn(createAccountResponse(TO_LOGIN, new BigDecimal("200.00")));

        List<Transfer> savedTransfers = new ArrayList<>();
        doAnswer(invocation -> {
            Transfer arg = invocation.getArgument(0);
            Transfer copy = Transfer.builder()
                    .id(arg.getId())
                    .fromLogin(arg.getFromLogin())
                    .toLogin(arg.getToLogin())
                    .amount(arg.getAmount())
                    .status(arg.getStatus())
                    .sagaStep(arg.getSagaStep())
                    .actionAt(arg.getActionAt())
                    .build();
            savedTransfers.add(copy);
            return arg;
        }).when(transferRepository).save(any(Transfer.class));

        doNothing().when(notificationProducer).sendNotificationSync(any(NotificationRequestDto.class));

        ArgumentCaptor<NotificationRequestDto> notificationCaptor = ArgumentCaptor.forClass(NotificationRequestDto.class);

        String message = createOutboxMessage(TRANSFER_ID, "DEPOSIT", payload);

        // Act
        sagaOrchestrator.handleOutboxEvent(message, ack);

        // Assert
        assertEquals(3, savedTransfers.size());

        // Проверяем статусы
        assertThat(savedTransfers.get(0).getStatus()).isEqualTo(TransferStatus.DEPOSIT_PENDING);
        assertThat(savedTransfers.get(1).getStatus()).isEqualTo(TransferStatus.DEPOSIT_COMPLETED);
        assertThat(savedTransfers.get(2).getStatus()).isEqualTo(TransferStatus.TRANSFER_COMPLETED);
        assertThat(savedTransfers.get(2).getSagaStep()).isEqualTo(3);

        // Проверяем уведомление
        verify(notificationProducer, times(1)).sendNotificationSync(notificationCaptor.capture());
        NotificationRequestDto notification = notificationCaptor.getValue();
        assertThat(notification.getSource()).isEqualTo(NotificationSource.TRANSFER_SERVICE);
        assertThat(notification.getMessage()).contains("Transfer completed");
        assertThat(notification.getMessage()).contains(FROM_LOGIN);
        assertThat(notification.getMessage()).contains(TO_LOGIN);
        assertThat(notification.getMessage()).containsPattern("amount=100[,\\.]00");

        verify(ack).acknowledge();
        verify(accountClient, times(1)).deposit(anyString(), any(DepositRequestDto.class));
        verify(outboxRepository, never()).save(any(TransferOutbox.class));
    }

    @Test
    @DisplayName("DEPOSIT: ошибка зачисления: COMPENSATE в outbox, в топик НЕ шлем")
    void shouldPublishCompensateWhenDepositFails() throws Exception {
        // Arrange
        Transfer transfer = createTransfer(TransferStatus.WITHDRAW_COMPLETED);
        JsonNode payload = createDepositPayload(TO_LOGIN, AMOUNT);

        when(transferRepository.findById(TRANSFER_ID)).thenReturn(Optional.of(transfer));
        when(accountClient.deposit(anyString(), any(DepositRequestDto.class)))
                .thenThrow(new RuntimeException("Account not found"));

        List<Transfer> savedTransfers = new ArrayList<>();
        doAnswer(invocation -> {
            Transfer arg = invocation.getArgument(0);
            Transfer copy = Transfer.builder()
                    .id(arg.getId())
                    .fromLogin(arg.getFromLogin())
                    .toLogin(arg.getToLogin())
                    .amount(arg.getAmount())
                    .status(arg.getStatus())
                    .sagaStep(arg.getSagaStep())
                    .actionAt(arg.getActionAt())
                    .build();
            savedTransfers.add(copy);
            return arg;
        }).when(transferRepository).save(any(Transfer.class));

        ArgumentCaptor<TransferOutbox> outboxCaptor = ArgumentCaptor.forClass(TransferOutbox.class);

        String message = createOutboxMessage(TRANSFER_ID, "DEPOSIT", payload);

        // Act
        sagaOrchestrator.handleOutboxEvent(message, ack);

        // Assert
        assertEquals(2, savedTransfers.size());
        assertThat(savedTransfers.get(0).getStatus()).isEqualTo(TransferStatus.DEPOSIT_PENDING);
        assertThat(savedTransfers.get(1).getStatus()).isEqualTo(TransferStatus.DEPOSIT_FAILED);

        // Проверяем создание Outbox для COMPENSATE
        verify(outboxRepository, times(1)).save(outboxCaptor.capture());
        TransferOutbox outbox = outboxCaptor.getValue();
        assertThat(outbox.getTransferId()).isEqualTo(TRANSFER_ID);
        assertThat(outbox.getEventType()).isEqualTo("COMPENSATE");
        assertThat(outbox.getPayload()).contains(TO_LOGIN);

        verify(ack).acknowledge();
        verify(accountClient, times(1)).deposit(anyString(), any(DepositRequestDto.class));
        verify(notificationProducer, never()).sendNotificationSync(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("COMPENSATE: успешно возвращает деньги и оправка в топик уведомления Transfer FAILED")
    void shouldProcessCompensateSuccessfully() throws Exception {
        // Arrange
        Transfer transfer = createTransfer(TransferStatus.DEPOSIT_FAILED);
        transfer.setFromLogin(FROM_LOGIN);
        JsonNode payload = createWithdrawPayload(FROM_LOGIN, AMOUNT);

        when(transferRepository.findById(TRANSFER_ID)).thenReturn(Optional.of(transfer));
        when(accountClient.deposit(anyString(), any(DepositRequestDto.class)))
                .thenReturn(createAccountResponse(FROM_LOGIN, new BigDecimal("1000.00")));

        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);

        String message = createOutboxMessage(TRANSFER_ID, "COMPENSATE", payload);

        // Act
        sagaOrchestrator.handleOutboxEvent(message, ack);

        // Assert
        verify(transferRepository, times(1)).save(transferCaptor.capture());

        Transfer savedTransfer = transferCaptor.getValue();
        assertThat(savedTransfer.getStatus()).isEqualTo(TransferStatus.TRANSFER_COMPENSATED);
        assertThat(savedTransfer.getSagaStep()).isEqualTo(0);

        verify(ack).acknowledge();
        verify(accountClient, times(1)).deposit(anyString(), any(DepositRequestDto.class));
        verify(notificationProducer, times(1)).sendNotificationSync(any(NotificationRequestDto.class));
        verify(outboxRepository, never()).save(any(TransferOutbox.class));
    }

    @Test
    @DisplayName("WITHDRAW: повторное сообщение не ломает состояние (уже COMPLETED)")
    void shouldSkipWithdrawWhenAlreadyCompleted() throws Exception {
        // Arrange
        Transfer transfer = createTransfer(TransferStatus.WITHDRAW_COMPLETED);
        JsonNode payload = createWithdrawPayload(FROM_LOGIN, AMOUNT);

        when(transferRepository.findById(TRANSFER_ID)).thenReturn(Optional.of(transfer));

        String message = createOutboxMessage(TRANSFER_ID, "WITHDRAW", payload);

        // Act
        sagaOrchestrator.handleOutboxEvent(message, ack);

        // Assert
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(accountClient, never()).withdraw(anyString(), any(WithdrawRequestDto.class));
        verify(outboxRepository, never()).save(any(TransferOutbox.class));
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("WITHDRAW: повторное сообщение не ломает состояние (уже PENDING)")
    void shouldSkipWithdrawWhenAlreadyPending() throws Exception {
        // Arrange
        Transfer transfer = createTransfer(TransferStatus.WITHDRAW_PENDING);
        JsonNode payload = createWithdrawPayload(FROM_LOGIN, AMOUNT);

        when(transferRepository.findById(TRANSFER_ID)).thenReturn(Optional.of(transfer));

        String message = createOutboxMessage(TRANSFER_ID, "WITHDRAW", payload);

        // Act
        sagaOrchestrator.handleOutboxEvent(message, ack);

        // Assert
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(accountClient, never()).withdraw(anyString(), any(WithdrawRequestDto.class));
        verify(outboxRepository, never()).save(any(TransferOutbox.class));
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("Плохой JSON: permanent error, ack")
    void shouldAckWhenInvalidJson() throws Exception {
        // Arrange
        String invalidMessage = "invalid json";

        // Act
        sagaOrchestrator.handleOutboxEvent(invalidMessage, ack);

        // Assert
        verify(ack).acknowledge();
        verify(transferRepository, never()).findById(any());
        verify(accountClient, never()).withdraw(anyString(), any(WithdrawRequestDto.class));
        verify(accountClient, never()).deposit(anyString(), any(DepositRequestDto.class));
        verify(outboxRepository, never()).save(any(TransferOutbox.class));
    }

    @Test
    @DisplayName("Нет поля 'after': permanent error, ack")
    void shouldAckWhenNoAfterField() throws Exception {
        // Arrange
        String message = "{\"before\":{}}";

        // Act
        sagaOrchestrator.handleOutboxEvent(message, ack);

        // Assert
        verify(ack).acknowledge();
        verify(transferRepository, never()).findById(any());
        verify(accountClient, never()).withdraw(anyString(), any(WithdrawRequestDto.class));
        verify(accountClient, never()).deposit(anyString(), any(DepositRequestDto.class));
        verify(outboxRepository, never()).save(any(TransferOutbox.class));
    }

    @Test
    @DisplayName("Не найден Transfer: permanent error, ack")
    void shouldAckWhenTransferNotFound() throws Exception {
        // Arrange
        JsonNode payload = createWithdrawPayload(FROM_LOGIN, AMOUNT);
        when(transferRepository.findById(TRANSFER_ID)).thenReturn(Optional.empty());

        String message = createOutboxMessage(TRANSFER_ID, "WITHDRAW", payload);

        // Act
        sagaOrchestrator.handleOutboxEvent(message, ack);

        // Assert
        verify(ack).acknowledge();
        verify(transferRepository).findById(TRANSFER_ID);
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(accountClient, never()).withdraw(anyString(), any(WithdrawRequestDto.class));
        verify(outboxRepository, never()).save(any(TransferOutbox.class));
    }

    @Test
    @DisplayName("Неизвестный eventType: permanent error, ack")
    void shouldAckWhenUnknownEventType() throws Exception {
        // Arrange
        Transfer transfer = createTransfer(TransferStatus.WITHDRAW_PENDING);
        JsonNode payload = createWithdrawPayload(FROM_LOGIN, AMOUNT);

        when(transferRepository.findById(TRANSFER_ID)).thenReturn(Optional.of(transfer));

        String message = createOutboxMessage(TRANSFER_ID, "UNKNOWN", payload);

        // Act
        sagaOrchestrator.handleOutboxEvent(message, ack);

        // Assert
        verify(ack).acknowledge();
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(accountClient, never()).withdraw(anyString(), any(WithdrawRequestDto.class));
        verify(accountClient, never()).deposit(anyString(), any(DepositRequestDto.class));
        verify(outboxRepository, never()).save(any(TransferOutbox.class));
    }

    private void assertWithdrawPayload(
            TransferOutbox outbox,
            String expectedLogin,
            BigDecimal expectedAmount) throws Exception {

        JsonNode payload = objectMapper.readTree(outbox.getPayload());
        assertThat(payload.get("type").asText()).isEqualTo("withdraw");
        assertThat(payload.get("fromLogin").asText()).isEqualTo(expectedLogin);
        assertThat(payload.get("amount").asDouble()).isEqualTo(expectedAmount.doubleValue());
    }

    private Transfer createTransfer(TransferStatus status) {
        return Transfer.builder()
                .id(TRANSFER_ID)
                .fromLogin(FROM_LOGIN)
                .toLogin(TO_LOGIN)
                .amount(AMOUNT)
                .status(status)
                .actionAt(Instant.now())
                .build();
    }

    private JsonNode createWithdrawPayload(String login, BigDecimal amount) throws Exception {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("type", "withdraw");
        payload.put("fromLogin", login);
        payload.put("amount", amount.doubleValue());
        return payload;
    }

    private JsonNode createDepositPayload(String login, BigDecimal amount) throws Exception {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("type", "deposit");
        payload.put("toLogin", login);
        payload.put("amount", amount.doubleValue());
        return payload;
    }

    private String createOutboxMessage(Long transferId, String eventType, JsonNode payload) throws Exception {
        ObjectNode after = objectMapper.createObjectNode();
        after.put("transfer_id", transferId);
        after.put("event_type", eventType);
        after.put("payload", objectMapper.writeValueAsString(payload));

        ObjectNode root = objectMapper.createObjectNode();
        root.set("after", after);

        return objectMapper.writeValueAsString(root);
    }

    private AccountResponseDto createAccountResponse(String login, BigDecimal balance) {
        return AccountResponseDto.builder()
                .login(login)
                .balance(balance)
                .build();
    }
}
