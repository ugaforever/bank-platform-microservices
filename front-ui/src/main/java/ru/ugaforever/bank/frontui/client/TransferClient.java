package ru.ugaforever.bank.frontui.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class TransferClient {

    @Qualifier("transferWebClient")
    private final WebClient transferWebClient;

    /*public void callTransferService() {
        transferWebClient.get()
                .uri("/transfer")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }*/
}
