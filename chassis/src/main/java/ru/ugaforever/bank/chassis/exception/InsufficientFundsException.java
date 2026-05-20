package ru.ugaforever.bank.chassis.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends BusinessRuleException {
    public InsufficientFundsException(String login, BigDecimal balance, BigDecimal requested) {
        super(String.format("Insufficient funds for account %s. Balance: %.2f, Requested: %.2f",
                login, balance, requested));
    }
}
