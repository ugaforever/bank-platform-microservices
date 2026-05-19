package ru.ugaforever.bank.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;
import ru.ugaforever.bank.chassis.exception.ValidationException;
import ru.ugaforever.bank.frontui.client.GatewayClient;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final GatewayClient gatewayClient;

    public AccountResponseDto getAccount(Long id){
        log.info("Получение аккаунта: id={}", id);

        return gatewayClient.getAccount(id);
    }

    public AccountResponseDto patchAccount(Long id, AccountUpdateDto update) {
        log.info("Обновление аккаунта: id={}, name={}, birthdate={}", id, update.getName(), update.getBirthdate());

        // TODO: проверки через аннотации в DTO
        if (update.getName() == null || update.getName().isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }

        if (update.getBirthdate() != null && update.getBirthdate().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        return gatewayClient.patchAccount(id, update);
    }
}
