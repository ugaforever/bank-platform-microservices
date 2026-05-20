package ru.ugaforever.bank.chassis.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.chassis.config.FeignConfig;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;

@FeignClient(
        name = "account-service",
        url = "${account.service.url:http://localhost:9005}",
        configuration = FeignConfig.class
)
public interface AccountClient {

    @GetMapping("/account/{login}")
    AccountResponseDto getAccount(@PathVariable("login") String login);

    @PatchMapping("/account/{login}")
    AccountResponseDto patchAccount(@PathVariable("login") String login,
                                    @RequestBody AccountUpdateDto updateDto);

    @PostMapping("/account/{login}/deposit")
    AccountResponseDto deposit(@PathVariable("login") String login,
                               @RequestBody DepositRequestDto request);

    @PostMapping("/account/{login}/withdraw")
    AccountResponseDto withdraw(@PathVariable("login") String login,
                                @RequestBody WithdrawRequestDto request);
}
