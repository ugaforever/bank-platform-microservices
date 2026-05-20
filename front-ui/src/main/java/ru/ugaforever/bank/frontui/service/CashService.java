package ru.ugaforever.bank.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;
import ru.ugaforever.bank.chassis.client.GatewayClient;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashService {

    private final GatewayClient gatewayClient;
    private final AccountService accountService;

    public CashResponseDto withdraw(String login, int value) {
        log.info("Withdraw cash: login={}, value={},", login, value);

        AccountResponseDto account = accountService.getAccount("ivanov");
        WithdrawRequestDto dto = WithdrawRequestDto.builder()
                .login(account.getLogin())
                .amount(BigDecimal.valueOf(value))
                .build();

        return gatewayClient.withdraw(dto);
    }

    public CashResponseDto deposit(String login, int value) {
        log.info("Deposit cash: login={}, value={},", login, value);

        AccountResponseDto account = accountService.getAccount("ivanov");
        DepositRequestDto dto = DepositRequestDto.builder()
                .login(account.getLogin())
                .amount(BigDecimal.valueOf(value))
                .build();

        return gatewayClient.deposit(dto);
    }
}
