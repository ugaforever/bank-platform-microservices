package ru.ugaforever.bank.frontui.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;


import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CashClient {

    @Qualifier("cashWebClient")
    private final WebClient cashWebClient;

    public CashResponseDto withdraw(AccountResponseDto account, int value){
        WithdrawRequestDto request = WithdrawRequestDto.builder()
                .accountId(account.getId())
                .amount(BigDecimal.valueOf(value))
                .build();

        // TODO: согласовать DTO и URLs
        return cashWebClient.post()
                .uri("/cash/{id}", account.getId())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CashResponseDto.class)
                .block();
    }

    public CashResponseDto deposit(AccountResponseDto account, int value) {
        DepositRequestDto request = DepositRequestDto.builder()
                .accountId(account.getId())
                .amount(BigDecimal.valueOf(value))
                .build();

        // TODO: согласовать DTO и URLs
        return cashWebClient.post()
                .uri("/cash/{id}", account.getId())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CashResponseDto.class)
                .block();
    }
}
