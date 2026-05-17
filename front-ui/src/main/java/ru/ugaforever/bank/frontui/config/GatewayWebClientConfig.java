package ru.ugaforever.bank.frontui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayWebClientConfig {

    @Value("${gateway.url:http://localhost:9001}")
    private String gatewayUrl;

    @Bean(name = "gatewayWebClient")
    public WebClient gatewayWebClient() {
        return WebClient.builder()
                .baseUrl(gatewayUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean(name = "accountWebClient")
    public WebClient accountWebClient(@Value("${account.service.url:http://localhost:9001}") String url) {
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean(name = "transferWebClient")
    public WebClient transferWebClient(@Value("${transfer.service.url:http://localhost:9001}") String url) {
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
