package ru.ugaforever.bank.chassis.exception;

import org.springframework.http.HttpStatus;

// 403 Forbidden - Доступ запрещён
public class ForbiddenException extends BankApplicationException {
    public ForbiddenException(String message) {
        super(message, "AUTH_002", HttpStatus.FORBIDDEN);
    }
}
