package ru.ugaforever.bank.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;
import ru.ugaforever.bank.frontui.client.GatewayClient;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashService {

    private final GatewayClient gatewayClient;
    private final AccountService accountService;

    public CashResponseDto withdraw(String login, int value) {
        log.info("Снятие виртуальных денег: login={}, value={},", login, value);

        //бизнес-логика

        // TODO: account-service переделать на поиск по login вместо id
        AccountResponseDto account = accountService.getAccount(1L);
        WithdrawRequestDto dto = WithdrawRequestDto.builder()
                .accountId(account.getId())
                .amount(BigDecimal.valueOf(value))
                .build();

        return gatewayClient.withdraw(dto);
    }

    public CashResponseDto deposit(String login, int value) {
        log.info("Внесение виртуальных денег: login={}, value={},", login, value);

        // TODO: account-service переделать на поиск по login вместо id
        AccountResponseDto account = accountService.getAccount(1L);
        DepositRequestDto dto = DepositRequestDto.builder()
                .accountId(account.getId())
                .amount(BigDecimal.valueOf(value))
                .build();

        return gatewayClient.deposit(dto);
    }
}
