package ru.ugaforever.bank.chassis.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

// 400 Bad Request
public class ValidationException extends BankApplicationException {
    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(message, "VALIDATION_001", HttpStatus.BAD_REQUEST);
        this.errors = null;
    }

    public ValidationException(Map<String, String> errors) {
        super("Validation failed", "VALIDATION_001", HttpStatus.BAD_REQUEST);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
