package ru.ugaforever.bank.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.account.dto.AccountRequestDto;
import ru.ugaforever.bank.account.dto.AccountResponseDto;
import ru.ugaforever.bank.account.service.AccountService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    //@PreAuthorize ??
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDto create(@RequestBody @Valid AccountRequestDto dto) {
        return accountService.createAccount(dto);
    }

    //curl -X GET http://localhost:9005/accounts/1
    @GetMapping("/{id}")
    //@PreAuthorize ??
    public AccountResponseDto get(@PathVariable Long id) {
        return accountService.getAccount(id);
    }
}
