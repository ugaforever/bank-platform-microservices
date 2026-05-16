package ru.ugaforever.bank.chassis.exception;

import org.springframework.http.HttpStatus;

public class BankApplicationException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public BankApplicationException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public BankApplicationException(String message, String errorCode, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
