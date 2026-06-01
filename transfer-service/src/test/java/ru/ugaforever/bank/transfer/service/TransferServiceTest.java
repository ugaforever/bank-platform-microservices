package ru.ugaforever.bank.transfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ugaforever.bank.chassis.client.AccountClient;
import ru.ugaforever.bank.chassis.client.NotificationClient;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountClient accountClient;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private TransferMapper mapper;

    @InjectMocks
    private TransferService transferService;

    private TransferRequestDto validRequest;
    private AccountResponseDto accountFrom;
    private Transfer savedTransfer;
    private TransferResponseDto expectedResponse;

    @BeforeEach
    void setUp() {
        validRequest = TransferRequestDto.builder()
                .fromLogin("user1")
                .toLogin("user2")
                .amount(new BigDecimal("100.50"))
                .build();

        accountFrom = AccountResponseDto.builder()
                .login("user1")
                .balance(new BigDecimal("500.00"))
                .build();

        savedTransfer = Transfer.builder()
                .id(1L)
                .fromLogin("user1")
                .toLogin("user2")
                .amount(new BigDecimal("100.50"))
                .status(TransferStatus.PENDING)
                .sagaStep(0)
                .build();

        expectedResponse = TransferResponseDto.builder()
                .id(1L)
                .fromLogin("user1")
                .toLogin("user2")
                .amount(new BigDecimal("100.50"))
                .status(TransferStatus.PENDING)
                .build();
    }

    @Test
    void submit_WithValidRequest_ShouldCreateTransferAndOutbox() {
        // Arrange
        when(accountClient.getAccount("user1")).thenReturn(accountFrom);
        when(transferRepository.save(any(Transfer.class))).thenReturn(savedTransfer);
        when(outboxRepository.save(any(TransferOutbox.class))).thenReturn(null);
        when(mapper.toDto(savedTransfer)).thenReturn(expectedResponse);

        // Act
        TransferResponseDto result = transferService.submit(validRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(TransferStatus.PENDING);

        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(transferCaptor.capture());
        Transfer capturedTransfer = transferCaptor.getValue();
        assertThat(capturedTransfer.getFromLogin()).isEqualTo("user1");
        assertThat(capturedTransfer.getToLogin()).isEqualTo("user2");
        assertThat(capturedTransfer.getAmount()).isEqualTo(new BigDecimal("100.50"));
        assertThat(capturedTransfer.getStatus()).isEqualTo(TransferStatus.PENDING);
        assertThat(capturedTransfer.getSagaStep()).isEqualTo(0);

        ArgumentCaptor<TransferOutbox> outboxCaptor = ArgumentCaptor.forClass(TransferOutbox.class);
        verify(outboxRepository).save(outboxCaptor.capture());
        TransferOutbox capturedOutbox = outboxCaptor.getValue();
        assertThat(capturedOutbox.getTransferId()).isEqualTo(1L);
        assertThat(capturedOutbox.getEventType()).isEqualTo("WITHDRAW");
        assertThat(capturedOutbox.getPayload()).contains("\"transferId\":1");
        assertThat(capturedOutbox.getPayload()).contains("\"fromLogin\":\"user1\"");
        assertThat(capturedOutbox.getPayload()).contains("\"toLogin\":\"user2\"");
        assertThat(capturedOutbox.getPayload()).contains("\"amount\":100.5");

        verify(mapper).toDto(savedTransfer);
    }

    @Test
    void submit_WithZeroAmount_ShouldThrowValidationException() {
        // Arrange
        validRequest.setAmount(BigDecimal.ZERO);

        // Act & Assert
        assertThatThrownBy(() -> transferService.submit(validRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("The amount must be greater than 0");

        verify(accountClient, never()).getAccount(anyString());
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(outboxRepository, never()).save(any(TransferOutbox.class));
    }

    @Test
    void submit_WithNegativeAmount_ShouldThrowValidationException() {
        // Arrange
        validRequest.setAmount(new BigDecimal("-50.00"));

        // Act & Assert
        assertThatThrownBy(() -> transferService.submit(validRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("The amount must be greater than 0");

        verify(accountClient, never()).getAccount(anyString());
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(outboxRepository, never()).save(any(TransferOutbox.class));
    }

    @Test
    void submit_WithInsufficientFunds_ShouldThrowBusinessRuleException() {
        // Arrange
        accountFrom.setBalance(new BigDecimal("50.00"));
        when(accountClient.getAccount("user1")).thenReturn(accountFrom);

        // Act & Assert
        assertThatThrownBy(() -> transferService.submit(validRequest))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Insufficient funds");

        verify(accountClient).getAccount("user1");
        verify(transferRepository, never()).save(any(Transfer.class));
        verify(outboxRepository, never()).save(any(TransferOutbox.class));
    }

    @Test
    void submit_WhenAccountNotFound_ShouldThrowException() {
        // Arrange
        when(accountClient.getAccount("user1")).thenThrow(new RuntimeException("Account not found"));

        // Act & Assert
        assertThatThrownBy(() -> transferService.submit(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Account not found");

        verify(transferRepository, never()).save(any(Transfer.class));
        verify(outboxRepository, never()).save(any(TransferOutbox.class));
    }

    @Test
    void submit_WithExactlyEqualBalance_ShouldSucceed() {
        // Arrange
        accountFrom.setBalance(new BigDecimal("100.50"));
        when(accountClient.getAccount("user1")).thenReturn(accountFrom);
        when(transferRepository.save(any(Transfer.class))).thenReturn(savedTransfer);
        when(outboxRepository.save(any(TransferOutbox.class))).thenReturn(null);
        when(mapper.toDto(savedTransfer)).thenReturn(expectedResponse);

        // Act
        TransferResponseDto result = transferService.submit(validRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(accountClient).getAccount("user1");
        verify(transferRepository).save(any(Transfer.class));
        verify(outboxRepository).save(any(TransferOutbox.class));
    }

    @Test
    void submit_ShouldCreateOutboxWithCorrectJsonFormat() {
        // Arrange
        when(accountClient.getAccount("user1")).thenReturn(accountFrom);
        when(transferRepository.save(any(Transfer.class))).thenReturn(savedTransfer);
        when(outboxRepository.save(any(TransferOutbox.class))).thenReturn(null);
        when(mapper.toDto(savedTransfer)).thenReturn(expectedResponse);

        // Act
        transferService.submit(validRequest);

        // Assert
        ArgumentCaptor<TransferOutbox> outboxCaptor = ArgumentCaptor.forClass(TransferOutbox.class);
        verify(outboxRepository).save(outboxCaptor.capture());

        String payload = outboxCaptor.getValue().getPayload();
        assertThat(payload).containsOnlyOnce("transferId");
        assertThat(payload).containsOnlyOnce("fromLogin");
        assertThat(payload).containsOnlyOnce("toLogin");
        assertThat(payload).containsOnlyOnce("amount");

        assertThat(payload.trim()).startsWith("{");
        assertThat(payload.trim()).endsWith("}");
    }
}
