package ru.ugaforever.bank.cash.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.cash.config.FeignConfig;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;

@FeignClient(
        name = "account-service",
        url = "${account.service.url:http://notification-service:9005}",
        configuration = FeignConfig.class
)
public interface AccountClient {

    @GetMapping("/account/{id}")
    AccountResponseDto getAccount(@PathVariable("id") Long id);

    @PatchMapping("/account/{id}")
    AccountResponseDto patchAccount(@PathVariable("id") Long id,
                                    @RequestBody AccountUpdateDto updateDto);
}
