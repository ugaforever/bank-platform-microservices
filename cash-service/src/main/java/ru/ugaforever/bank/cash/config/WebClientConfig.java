package ru.ugaforever.bank.cash.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean(name = "accountWebClient")
    public WebClient accountWebClient(@Value("${account.service.url:http://localhost:9001}") String url) {
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean(name = "notificationWebClient")
    public WebClient transferWebClient(@Value("${notification.service.url:http://localhost:9006}") String url) {
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
