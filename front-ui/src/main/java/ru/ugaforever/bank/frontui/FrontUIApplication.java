package ru.ugaforever.bank.frontui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.ugaforever.bank.chassis.advice.GlobalExceptionHandler;
import ru.ugaforever.bank.chassis.client.GatewayClient;
import ru.ugaforever.bank.chassis.config.FeignConfig;

@SpringBootApplication
@EnableFeignClients(
        clients = {GatewayClient.class},
        defaultConfiguration = FeignConfig.class
)
@Import(GlobalExceptionHandler.class)
public class FrontUIApplication {
    public static void main(String[] args) {
        SpringApplication.run(FrontUIApplication.class, args);
    }
}

