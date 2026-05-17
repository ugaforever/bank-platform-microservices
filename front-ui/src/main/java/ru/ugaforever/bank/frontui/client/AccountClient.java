package ru.ugaforever.bank.frontui.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ugaforever.bank.chassis.dto.AccountResponseDto;

@Component
@RequiredArgsConstructor
public class AccountClient {

    @Qualifier("accountWebClient")
    private final WebClient accountWebClient;

    public AccountResponseDto getAccount(Long id) {
        return accountWebClient.get()
                .uri("/account/{id}", id)
                .retrieve()
                .bodyToMono(AccountResponseDto.class)
                .block();
    }
}
