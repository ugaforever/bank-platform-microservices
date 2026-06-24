package ru.ugaforever.bank.cash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import ru.ugaforever.bank.chassis.advice.GlobalExceptionHandler;
import ru.ugaforever.bank.chassis.client.AccountClient;
import ru.ugaforever.bank.chassis.config.FeignConfig;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(
        clients = {AccountClient.class},
        defaultConfiguration = FeignConfig.class
)
@EnableRetry
@Import(GlobalExceptionHandler.class)
public class CashApplication {
    public static void main(String[] args) {
        SpringApplication.run(CashApplication.class, args);
    }
}
