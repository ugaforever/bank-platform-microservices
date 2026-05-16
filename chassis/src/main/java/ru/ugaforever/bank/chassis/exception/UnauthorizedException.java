package ru.ugaforever.bank.chassis.exception;

import org.springframework.http.HttpStatus;

// 401 Unauthorized - Не авторизован
public class UnauthorizedException extends BankApplicationException {
    public UnauthorizedException(String message) {
        super(message, "AUTH_001", HttpStatus.UNAUTHORIZED);
    }
}