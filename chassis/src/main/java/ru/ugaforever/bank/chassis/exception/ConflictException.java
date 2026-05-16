package ru.ugaforever.bank.chassis.exception;

import org.springframework.http.HttpStatus;

// 409
public class ConflictException extends BankApplicationException {
    public ConflictException(String message) {
        super(message, "CONFLICT_001", HttpStatus.CONFLICT);
    }
}
