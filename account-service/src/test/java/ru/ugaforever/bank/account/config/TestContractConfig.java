package ru.ugaforever.bank.account.config;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.ugaforever.bank.chassis.client.NotificationClient;


@TestConfiguration
public class TestContractConfig {

    @Bean
    public NotificationClient notificationClient() {
        // можно вернуть мок, но для реального вызова stub runner нужен Feign
        // лучше вернуть Feign.Builder с явно указанным URL
        return Feign.builder()
                .decoder(new JacksonDecoder())
                .target(NotificationClient.class, "http://localhost:${stubrunner.stubs.port}");
    }
}
