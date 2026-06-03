package ru.ugaforever.bank.chassis.exception;

import org.springframework.http.HttpStatus;

// 422 Unprocessable Entity
public class BusinessRuleException extends BankApplicationException {
    public BusinessRuleException(String message) {
        super(message, "BUSINESS_001", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
