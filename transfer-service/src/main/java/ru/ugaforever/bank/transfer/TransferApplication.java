package ru.ugaforever.bank.transfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import ru.ugaforever.bank.chassis.advice.GlobalExceptionHandler;
import ru.ugaforever.bank.chassis.client.AccountClient;
import ru.ugaforever.bank.chassis.config.FeignConfig;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(
        clients = {AccountClient.class},
        defaultConfiguration = FeignConfig.class
)
@Import(GlobalExceptionHandler.class)
@EnableKafka
public class TransferApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransferApplication.class, args);
    }
}
