package ru.ugaforever.bank.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;
import ru.ugaforever.bank.chassis.exception.ValidationException;
import ru.ugaforever.bank.chassis.client.GatewayClient;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final GatewayClient gatewayClient;

    public AccountResponseDto getAccount(String login){
        log.info("Get account: login={}", login);

        return gatewayClient.getAccount(login);
    }

    public AccountResponseDto patchAccount(String login, AccountUpdateDto update) {
        log.info("Patch account: login={}, name={}, birthdate={}", login, update.getName(), update.getBirthdate());

        // TODO: проверки через аннотации в DTO
        if (update.getName() == null || update.getName().isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }

        if (update.getBirthdate() != null && update.getBirthdate().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        return gatewayClient.patchAccount(login, update);
    }

    public List<AccountResponseDto> getAllAccounts() {
        log.info("Get all accounts");

        return gatewayClient.getAllAccount();
    }
}
