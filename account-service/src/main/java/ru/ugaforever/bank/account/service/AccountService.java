package ru.ugaforever.bank.account.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ugaforever.bank.account.dto.AccountRequestDto;
import ru.ugaforever.bank.account.dto.AccountResponseDto;
import ru.ugaforever.bank.account.exception.AccountNotFoundException;
import ru.ugaforever.bank.account.mapper.AccountMapper;
import ru.ugaforever.bank.account.model.Account;
import ru.ugaforever.bank.account.repository.AccountRepository;

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
                    return new AccountNotFoundException("Аккаунт ID " + id + " не найден");
                });
    }
}
