package ru.ugaforever.bank.transfer.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.transfer.client.AccountClient;
import ru.ugaforever.bank.transfer.dto.TransferRequestDto;
import ru.ugaforever.bank.transfer.mapper.TransferMapper;
import ru.ugaforever.bank.transfer.repository.TransferRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final TransferRepository repository;
    private final TransferMapper mapper;

    private final AccountClient accountsClient;


    public String preview() {
        log.debug("Запрошен предпросмотр перевода");
        return "Предпросмотр перевода: всё выглядит корректно.";
    }

    public String submit(TransferRequestDto request, JwtAuthenticationToken authentication) {
        String username = authentication.getToken().getClaimAsString("preferred_username");

        log.info("Запрос перевода: user={}, from={}, to={}, amount={}",
                username, request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        boolean owner = accountsClient.isOwner(request.getFromAccountId(), username);
        log.debug("Проверка владельца счёта: user={}, from={}, isOwner={}",
                username, request.getFromAccountId(), owner);

        if (!owner) {
            log.warn("Отказ в переводе: пользователь {} не владелец счёта {}", username, request.getFromAccountId());
            throw new AccessDeniedException(
                    "Пользователь " + username + " не является владельцем счёта " + request.getFromAccountId()
            );
        }

        String result = accountsClient.transfer(request);
        log.info("Перевод выполнен успешно: {}", result);
        return result;
    }
}
