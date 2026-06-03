package ru.ugaforever.bank.chassis.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.chassis.config.FeignConfig;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferRequestDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferResponseDto;

import java.util.List;


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

    @GetMapping("/account/{login}")
    AccountResponseDto getAccount(@PathVariable("login") String login);

    @PatchMapping("/account/{login}")
    AccountResponseDto patchAccount(@PathVariable("login") String login,
                                    @RequestBody AccountUpdateDto updateDto);

    @GetMapping("/account")
    List<AccountResponseDto> getAllAccount();

    @PostMapping("/transfer")
    TransferResponseDto submit(TransferRequestDto requestDto);
}
