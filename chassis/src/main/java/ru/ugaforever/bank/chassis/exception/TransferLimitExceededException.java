package ru.ugaforever.bank.chassis.exception;

import java.math.BigDecimal;

// Transfer Service exceptions
public class TransferLimitExceededException extends BusinessRuleException {
    public TransferLimitExceededException(BigDecimal limit, BigDecimal requested) {
        super(String.format("Transfer limit exceeded. Max: %.2f, Requested: %.2f", limit, requested));
    }
}
