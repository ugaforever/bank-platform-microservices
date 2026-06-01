package ru.ugaforever.bank.chassis.advice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.ugaforever.bank.chassis.dto.ErrorResponse;
import ru.ugaforever.bank.chassis.exception.*;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleValidation(UnauthorizedException exception) {
        log.warn("Argument validation: {}", exception.getMessage());

        return ErrorResponse.of(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException exception) {
        log.warn("Argument validation: {}", exception.getMessage());

        Map<String, String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        return ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                exception.getMessage(),
                details);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException exception) {
        log.warn("Illegal argument: {}", exception.getMessage());

        return ErrorResponse.of(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                exception.getMessage());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleAccountNotFound(AccountNotFoundException exception) {
        log.warn("Account not found: {}", exception.getMessage());

        return ErrorResponse.of(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                exception.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException exception) {
        log.warn("Constraint violation: {}", exception.getMessage());

        return ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                exception.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException exception) {
        log.warn("Resource not found: {}", exception.getMessage());

        return ErrorResponse.of(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ConflictException exception) {
        log.warn("Conflict: {}", exception.getMessage());

        return ErrorResponse.of(
                HttpStatus.CONFLICT,
                "CONFLICT",
                exception.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleBusinessRule(BusinessRuleException exception) {
        log.warn("Business rule violation: {}", exception.getMessage());

        return ErrorResponse.of(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "UNPROCESSABLE_ENTITY",
                exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception exception) {
        log.error("Unexpected error", exception);

        return ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                exception.getMessage());
    }
}

