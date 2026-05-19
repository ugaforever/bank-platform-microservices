package ru.ugaforever.bank.transfer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;
import ru.ugaforever.bank.transfer.config.FeignConfig;

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
