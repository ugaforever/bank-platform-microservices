package ru.ugaforever.bank.account;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import ru.ugaforever.bank.chassis.advice.GlobalExceptionHandler;

@SpringBootApplication
@EnableDiscoveryClient
@Import(GlobalExceptionHandler.class)
public class AccountApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }
}
