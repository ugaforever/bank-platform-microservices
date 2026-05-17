package ru.ugaforever.bank.account.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.chassis.dto.AccountRequestDto;
import ru.ugaforever.bank.chassis.dto.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.AccountUpdateDto;
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

    public AccountResponseDto getAccount(Long id) {

        log.debug("Запрошен аккаунт: {}", id);

        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> {
                    log.warn("Аккаунт не найден: {}", id);
                    return new AccountNotFoundException(id);
                });
    }

    public AccountResponseDto updateAccount(Long id, AccountUpdateDto updateDto) {
        log.info("Обновление аккаунта: id={}, fields={}", id, updateDto);

        if (!updateDto.hasUpdates()) {
            throw new ValidationException("Не указаны поля для обновления");
        }

        Account account = repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

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
