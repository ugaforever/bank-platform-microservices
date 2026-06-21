/*
package ru.ugaforever.bank.chassis.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.ugaforever.bank.chassis.config.FeignConfig;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationResponseDto;

@FeignClient(
        name = "notification-service",
        url = "${notification.service.url:http://localhost:9006}",
        configuration = FeignConfig.class
)
public interface NotificationClient {

    @PostMapping("/notification")
    NotificationResponseDto sendNotification(@RequestBody NotificationRequestDto request);
}
*/
