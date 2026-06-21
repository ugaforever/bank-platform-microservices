package ru.ugaforever.bank.account;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.ugaforever.bank.chassis.advice.GlobalExceptionHandler;
import ru.ugaforever.bank.chassis.config.FeignConfig;

@SpringBootApplication
@EnableDiscoveryClient
@Import(GlobalExceptionHandler.class)
public class AccountApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }
}
