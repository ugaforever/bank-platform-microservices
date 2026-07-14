package ru.ugaforever.bank.cash.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ugaforever.bank.chassis.exception.IdempotencyKeyNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IdempotencyKeyUnitTest {

    private IdempotencyInterceptor interceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @BeforeEach
    void setUp() {
        interceptor = new IdempotencyInterceptor();
    }

    private static final String IDEMPOTENCY_KEY = "key";

    @Test
    @DisplayName("Должен возвращать значение true при наличии ключа идемпотентности")
    void shouldReturnTrueWhenKeyPresent() {
        when(request.getHeader("Idempotency-Key")).thenReturn(IDEMPOTENCY_KEY);

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result);
        assertEquals(IDEMPOTENCY_KEY, IdempotencyInterceptor.getCurrentIdempotencyKey());
    }

    @Test
    @DisplayName("Должен выбросить исключение при idempotency key = null")
    void shouldThrowExceptionWhenKeyMissing() {
        when(request.getHeader("Idempotency-Key")).thenReturn(null);

        assertThrows(IdempotencyKeyNotFoundException.class, () -> {
            interceptor.preHandle(request, response, handler);
        });
    }

    @Test
    @DisplayName("Должен выбросить исключение при idempotency key = '' ")
    void shouldThrowExceptionWhenKeyEmpty() {
        when(request.getHeader("Idempotency-Key")).thenReturn("");

        assertThrows(IdempotencyKeyNotFoundException.class, () -> {
            interceptor.preHandle(request, response, handler);
        });
    }

    @Test
    @DisplayName("Должен выбросить исключение при idempotency key='    ' (blank)")
    void shouldThrowExceptionWhenKeyBlank() {
        when(request.getHeader("Idempotency-Key")).thenReturn("   ");

        assertThrows(IdempotencyKeyNotFoundException.class, () -> {
            interceptor.preHandle(request, response, handler);
        });
    }

    @Test
    @DisplayName("Должен очисить после afterCompletion")
    void shouldClearThreadLocalInAfterCompletion() {

        when(request.getHeader("Idempotency-Key")).thenReturn(IDEMPOTENCY_KEY);
        interceptor.preHandle(request, response, handler);
        assertEquals(IDEMPOTENCY_KEY, IdempotencyInterceptor.getCurrentIdempotencyKey());

        interceptor.afterCompletion(request, response, handler, null);

        assertNull(IdempotencyInterceptor.getCurrentIdempotencyKey());
    }

    @Test
    @DisplayName("Должен обрабатывать нулевое исключение в afterCompletion")
    void shouldHandleNullExceptionInAfterCompletion() {

        when(request.getHeader("Idempotency-Key")).thenReturn(IDEMPOTENCY_KEY);
        interceptor.preHandle(request, response, handler);

        // Вызываем afterCompletion с null exception
        assertDoesNotThrow(() -> {
            interceptor.afterCompletion(request, response, handler, null);
        });

        assertNull(IdempotencyInterceptor.getCurrentIdempotencyKey());
    }
}
