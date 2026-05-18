package ru.ugaforever.bank.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.frontui.client.CashClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashService {

    private final CashClient cashClient;

    public CashResponseDto withdraw(AccountResponseDto account, int value) {
        log.info("Снятие виртуальных денег: id={}, value={},", account.getId(), value);

        //бизнес-логика на будущее

        return cashClient.withdraw(account, value);
    }

    public CashResponseDto deposit(AccountResponseDto account, int value) {
        log.info("Внесение виртуальных денег: id={}, value={},", account.getId(), value);

        return cashClient.deposit(account, value);
    }
}
