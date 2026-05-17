package ru.ugaforever.bank.frontui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TransferWebClientConfig {

    @Value("${transfer.service.url:http://localhost:9003}")
    private String transferServiceUrl;

    @Bean(name = "transferWebClient")
    public WebClient transferWebClient() {
        return WebClient.builder()
                .baseUrl(transferServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}