package ru.ugaforever.bank.transfer.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.ugaforever.bank.transfer.config.FeignConfig;

@FeignClient(
        name = "notification-service",
        url = "${notification.service.url:http://notification-service:9006}",
        configuration = FeignConfig.class
)
public interface NotificationClient {

    //@PostMapping("/notification/send")
    //void sendNotification(@RequestBody NotificationRequest request);
}
