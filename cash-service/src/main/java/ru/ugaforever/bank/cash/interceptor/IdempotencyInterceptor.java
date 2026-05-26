package ru.ugaforever.bank.cash.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class IdempotencyInterceptor implements HandlerInterceptor {

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final ThreadLocal<String> currentKey = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();

        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            currentKey.set(idempotencyKey);
            log.debug("Idempotency key captured: {} for {} {}",
                    idempotencyKey, method, request.getRequestURI());
        } else {
            log.warn("Idempotency key missing for {} {}", method, request.getRequestURI());
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        currentKey.remove();
    }

    public static String getCurrentIdempotencyKey() {
        return currentKey.get();
    }
}
