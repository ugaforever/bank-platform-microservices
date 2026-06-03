package ru.ugaforever.bank.chassis.dto.transfer;

public enum TransferStatus {
    PENDING,
    WITHDRAW_COMPLETED,
    DEPOSIT_COMPLETED,
    COMPLETED,
    FAILED,
    COMPENSATED
}
