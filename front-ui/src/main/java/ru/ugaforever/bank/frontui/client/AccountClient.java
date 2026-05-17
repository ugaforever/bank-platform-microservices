package ru.ugaforever.bank.frontui.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ugaforever.bank.chassis.dto.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.AccountUpdateDto;

import java.time.LocalDate;

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

    public AccountResponseDto patchAccount(Long id, String name, LocalDate birthdate) {

        AccountUpdateDto request = AccountUpdateDto.builder()
                .name(name)
                .birthdate(birthdate)
                .build();

        return accountWebClient.patch()
                .uri("/account/{id}", id)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AccountResponseDto.class)
                .block();
    }
}
