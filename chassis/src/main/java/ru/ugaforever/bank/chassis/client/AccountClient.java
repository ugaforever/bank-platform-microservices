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
                               @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                               @RequestBody DepositRequestDto request);

    default AccountResponseDto deposit(String login, DepositRequestDto request) {
        return deposit(login, null, request);
    }

    @PostMapping("/account/{login}/withdraw")
    AccountResponseDto withdraw(@PathVariable("login") String login,
                                @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                @RequestBody WithdrawRequestDto request);

    default AccountResponseDto withdraw(String login, WithdrawRequestDto request) {
        return withdraw(login, null, request);
    }
}
