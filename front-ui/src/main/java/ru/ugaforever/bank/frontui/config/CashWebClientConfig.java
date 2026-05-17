package ru.ugaforever.bank.frontui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class CashWebClientConfig {

    @Value("${cash.service.url:http://localhost:9004}")
    private String cashServiceUrl;

    @Bean(name = "cashWebClient")
    public WebClient cashWebClient() {
        return WebClient.builder()
                .baseUrl(cashServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

