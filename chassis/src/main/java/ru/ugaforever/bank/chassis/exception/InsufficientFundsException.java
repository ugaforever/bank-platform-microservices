package ru.ugaforever.bank.chassis.exception;

import java.math.BigDecimal;

// Cash Service exceptions
public class InsufficientFundsException extends BusinessRuleException {
    public InsufficientFundsException(Long accountId, BigDecimal balance, BigDecimal requested) {
        super(String.format("Insufficient funds for account %d. Balance: %.2f, Requested: %.2f",
                accountId, balance, requested));
    }
}
