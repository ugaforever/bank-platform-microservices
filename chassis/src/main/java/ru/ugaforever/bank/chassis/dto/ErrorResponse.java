package ru.ugaforever.bank.chassis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

// Общая структура ответа с ошибкой
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> details;
    // TODO: сделать 2 разновидности GlobalExceptionHandlerMVC, GlobalExceptionHandlerWebflux
    //private String path;
    //private String requestId;

    public static ErrorResponse of(HttpStatus status, String error, String message) {
        return ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse of(HttpStatus status, String error, String message, Map<String, String> details) {
        ErrorResponse response = of(status, error, message);
        response.setDetails(details);
        return response;
    }
}
