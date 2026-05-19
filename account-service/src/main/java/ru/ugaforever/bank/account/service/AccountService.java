package ru.ugaforever.bank.account.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.chassis.dto.account.AccountRequestDto;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;
import ru.ugaforever.bank.chassis.exception.AccountNotFoundException;
import ru.ugaforever.bank.account.mapper.AccountMapper;
import ru.ugaforever.bank.account.model.Account;
import ru.ugaforever.bank.account.repository.AccountRepository;
import ru.ugaforever.bank.chassis.exception.ValidationException;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository repository;
    private final AccountMapper mapper;

    public AccountResponseDto createAccount(AccountRequestDto dto) {
        Account account = mapper.toEntity(dto);
        Account saved = repository.save(account);
        return mapper.toDto(saved);
    }

    public AccountResponseDto getAccount(String login) {

        log.debug("Запрошен аккаунт: login={}", login);

        return repository.findByLogin(login)
                .map(mapper::toDto)
                .orElseThrow(() -> {
                    log.warn("Аккаунт не найден: {}", login);
                    return new AccountNotFoundException(login);
                });
    }

    public AccountResponseDto updateAccount(String login, AccountUpdateDto updateDto) {
        log.info("Обновление аккаунта: login={}, fields={}", login, updateDto);

        if (!updateDto.hasUpdates()) {
            throw new ValidationException("Не указаны поля для обновления");
        }

        Account account = repository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));

        if (updateDto.getName() != null) {
            account.setName(updateDto.getName());
        }

        if (updateDto.getBirthdate() != null) {
            account.setBirthdate(updateDto.getBirthdate());
        }

        Account saved = repository.save(account);
        log.info("Аккаунт обновлён: id={}", saved.getId());

        return mapper.toDto(saved);
    }

}
