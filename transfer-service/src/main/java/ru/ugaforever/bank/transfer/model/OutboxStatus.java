package ru.ugaforever.bank.transfer.model;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED,
    EXHAUSTED
}
