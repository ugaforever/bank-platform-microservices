package ru.ugaforever.bank.frontui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AccountWebClientConfig {

    @Value("${account.service.url:http://localhost:9005}")
    private String accountServiceUrl;

    @Bean(name = "accountWebClient")
    public WebClient accountWebClient() {
        return WebClient.builder()
                .baseUrl(accountServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
