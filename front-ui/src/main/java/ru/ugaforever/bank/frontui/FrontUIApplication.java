package ru.ugaforever.bank.frontui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.ugaforever.bank.chassis.client.GatewayClient;
import ru.ugaforever.bank.chassis.config.FeignConfig;

@SpringBootApplication
@EnableFeignClients(
        clients = {GatewayClient.class},
        defaultConfiguration = FeignConfig.class  // ✅ Подключаем
)
public class FrontUIApplication {
    public static void main(String[] args) {
        SpringApplication.run(FrontUIApplication.class, args);
    }
}

