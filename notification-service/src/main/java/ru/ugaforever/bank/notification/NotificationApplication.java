package ru.ugaforever.bank.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import ru.ugaforever.bank.chassis.advice.GlobalExceptionHandler;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@Import(GlobalExceptionHandler.class)
public class NotificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }
}
