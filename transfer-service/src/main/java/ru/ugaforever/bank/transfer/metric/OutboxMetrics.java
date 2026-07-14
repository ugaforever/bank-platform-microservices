package ru.ugaforever.bank.transfer.metric;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OutboxMetrics {

    private final Counter processedCounter;
    private final Counter failedCounter;

    public OutboxMetrics(MeterRegistry registry) {
        this.processedCounter = Counter.builder("outbox.messages.processed")
                .description("Number of successfully processed outbox messages")
                .register(registry);

        this.failedCounter = Counter.builder("outbox.messages.failed")
                .description("Number of failed outbox messages")
                .register(registry);
    }

    public void incrementProcessed() {
        processedCounter.increment();
    }

    public void incrementFailed() {
        failedCounter.increment();
    }
}
