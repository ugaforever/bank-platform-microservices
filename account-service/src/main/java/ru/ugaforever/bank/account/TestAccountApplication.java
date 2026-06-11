package ru.ugaforever.bank.account;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import ru.ugaforever.bank.chassis.client.NotificationClient;

import static org.mockito.Mockito.mock;


@SpringBootApplication(
        scanBasePackages = "ru.ugaforever.bank.account",
        exclude = {
                org.springframework.cloud.client.discovery.EnableDiscoveryClient.class,
                org.springframework.cloud.openfeign.FeignAutoConfiguration.class
        }
)
public class TestAccountApplication {
    @Bean
    @Primary
    public NotificationClient notificationClient() {
        return mock(NotificationClient.class);
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }
}
