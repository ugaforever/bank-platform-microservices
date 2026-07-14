package ru.ugaforever.bank.account;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import ru.ugaforever.bank.chassis.advice.GlobalExceptionHandler;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableRetry
@Import(GlobalExceptionHandler.class)
public class AccountApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }

    @PostConstruct
    public void checkConfig() {

        log.info("AccountApplication. This is a info log");
        log.warn("AccountApplication. This is a warning log");
        log.error("AccountApplication. This is an error log");
    }
}
