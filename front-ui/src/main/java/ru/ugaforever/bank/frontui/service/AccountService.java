package ru.ugaforever.bank.frontui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.frontui.client.AccountClient;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountClient accountClient;

    public AccountResponseDto getAccount(Long id){
        log.info("Получение аккаунта: id={}", id);

        //бизнес-логика на будущее

        return accountClient.getAccount(id);
    }

    public AccountResponseDto patchAccount(Long id, String name, LocalDate birthdate) {
        log.info("Обновление аккаунта: id={}, name={}, birthdate={}", id, name, birthdate);

        // бизнес-логика на будущее

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }

        if (birthdate != null && birthdate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем");
        }

        return accountClient.patchAccount(id, name, birthdate);
    }


}
