package ru.ugaforever.bank.frontui.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;
import ru.ugaforever.bank.frontui.config.FeignConfig;

@FeignClient(
        name = "gateway",
        url = "${gateway.url:http://localhost:9001}",
        configuration = FeignConfig.class
)
public interface GatewayClient {

    @PostMapping("/cash/deposit")
    CashResponseDto deposit(@RequestBody DepositRequestDto request);

    @PostMapping("/cash/withdraw")
    CashResponseDto withdraw(@RequestBody WithdrawRequestDto request);

    @GetMapping("/account/{id}")
    AccountResponseDto getAccount(@PathVariable("id") Long id);

    @PatchMapping("/account/{id}")
    AccountResponseDto patchAccount(@PathVariable("id") Long id,
                                    @RequestBody AccountUpdateDto updateDto);
}
