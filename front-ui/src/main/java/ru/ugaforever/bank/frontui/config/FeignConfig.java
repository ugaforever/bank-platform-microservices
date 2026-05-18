package ru.ugaforever.bank.frontui.config;

import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    // Для PATCH методов
    @Bean
    public Client feignClient() {
        return new ApacheHttpClient();
    }
}