package ru.ugaforever.bank.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(-1)
public class IdempotencyGatewayFilter implements GlobalFilter {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final long KEY_TTL_HOURS = 24;

    // TODO: заменить на Redis
    private final Map<String, Instant> processedKeys = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        cleanupExpiredKeys();

        ServerHttpRequest request = exchange.getRequest();

        // TODO: сейчас для всех методов, даже идемпотентых GET (тест)
        //HttpMethod method = request.getMethod();

        String idempotencyKey = request.getHeaders().getFirst(IDEMPOTENCY_KEY_HEADER);

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = UUID.randomUUID().toString();
        }

        if (processedKeys.containsKey(idempotencyKey)) {

            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.CONFLICT);
            return exchange.getResponse().setComplete();
        }

        processedKeys.put(idempotencyKey, Instant.now());
        exchange.getResponse().getHeaders().add(IDEMPOTENCY_KEY_HEADER, idempotencyKey);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(IDEMPOTENCY_KEY_HEADER, idempotencyKey)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private void cleanupExpiredKeys() {
        Instant expiryTime = Instant.now().minusSeconds(KEY_TTL_HOURS * 3600);
        processedKeys.entrySet().removeIf(entry -> entry.getValue().isBefore(expiryTime));
    }

}
