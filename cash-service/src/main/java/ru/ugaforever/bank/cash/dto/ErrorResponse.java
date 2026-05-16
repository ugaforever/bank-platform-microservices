package ru.ugaforever.bank.cash.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final String message;
    private final Map<String, String> errors;

    public ErrorResponse(String message) {
        this.message = message;
        this.errors = null;
    }
}
