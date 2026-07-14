package ru.ugaforever.bank.chassis.exception;

import org.springframework.http.HttpStatus;

public class IdempotencyKeyNotFoundException extends BankApplicationException {
    public IdempotencyKeyNotFoundException(String message) {
        super(message, "BUSINESS_002", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
