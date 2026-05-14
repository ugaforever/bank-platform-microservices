package ru.ugaforever.bank.frontui.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ugaforever.bank.frontui.model.TransferRequest;

import java.math.BigDecimal;

@Component
public class TransferClient {

    private final WebClient gatewayWebClient;
    private final String gatewayBaseUrl;

    public TransferClient(WebClient gatewayWebClient,
                          @Value("${bank.gateway.base-url}") String gatewayBaseUrl) {
        this.gatewayWebClient = gatewayWebClient;
        this.gatewayBaseUrl = gatewayBaseUrl;
    }

    public String previewTransfer() {
        return gatewayWebClient
                .get()
                .uri(gatewayBaseUrl + "/transfers/preview")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String submitTransfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(fromAccountId);
        request.setToAccountId(toAccountId);
        request.setAmount(amount);

        return gatewayWebClient
                .post()
                .uri(gatewayBaseUrl + "/transfers")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

