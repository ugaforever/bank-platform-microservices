package ru.ugaforever.bank.transfer.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.ugaforever.bank.transfer.dto.TransferRequestDto;

@Component
public class AccountClient {

    private final WebClient accountWebClient;

    public AccountClient(WebClient accountWebClient) {
        this.accountWebClient = accountWebClient;
    }

    public String transfer(TransferRequestDto request) {
        return accountWebClient
                .post()
                .uri("/account/transfer")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(throwable -> Mono.just("Ошибка при обращении к account-service: " + throwable.getMessage()))
                .block();
    }

    public boolean isOwner(Long accountId, String username) {

        return true;

        /*return Boolean.TRUE.equals(accountWebClient
                .get()
                .uri("/account/{accountId}/owner", accountId)
                .retrieve()
                .bodyToMono(AccountOwnerResponse.class)
                .map(resp -> username.equals(resp.getOwnerUsername()))
                .onErrorReturn(false)
                .block());*/
    }
}
