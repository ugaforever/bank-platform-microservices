package ru.ugaforever.bank.chassis.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.ugaforever.bank.chassis.config.FeignConfig;


@FeignClient(
        name = "notification-service",
        url = "${notification.service.url:http://localhost:9006}",
        configuration = FeignConfig.class
)
public interface NotificationClient {

    //@PostMapping("/notification/send")
    //void sendNotification(@RequestBody NotificationRequest request);
}
