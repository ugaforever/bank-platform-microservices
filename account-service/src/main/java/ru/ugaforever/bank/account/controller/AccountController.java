package ru.ugaforever.bank.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.account.service.AccountService;
import ru.ugaforever.bank.chassis.dto.account.AccountRequestDto;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;

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

    @GetMapping("/{id}")
    //@PreAuthorize ??
    public AccountResponseDto get(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    @PatchMapping("/{id}")
    //@PreAuthorize ??
    public AccountResponseDto patchAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateDto updateDto
    ) {

        return accountService.updateAccount(id, updateDto);
    }
}
