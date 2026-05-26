package ru.ugaforever.bank.chassis.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
@Builder
public class TransferResponseDto {
    private Long id;
    private String fromLogin;
    private String toLogin;
    private BigDecimal amount;
    private Instant actionAt;

    @Builder.Default
    private boolean success = true;
    private String errorMessage;
}
