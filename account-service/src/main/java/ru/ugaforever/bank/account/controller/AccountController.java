package ru.ugaforever.bank.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.account.service.AccountService;
import ru.ugaforever.bank.chassis.dto.account.AccountRequestDto;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;

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

    @GetMapping("/{login}")
    //@PreAuthorize ??
    public AccountResponseDto get(@PathVariable String login) {
        return accountService.getAccount(login);
    }

    @PatchMapping("/{login}")
    //@PreAuthorize ??
    public AccountResponseDto patchAccount(
            @PathVariable String login,
            @Valid @RequestBody AccountUpdateDto updateDto) {

        return accountService.updateAccount(login, updateDto);
    }

    @PostMapping("/{login}/deposit")
    //@PreAuthorize ??
    public AccountResponseDto deposit(
            @PathVariable String login,
            @RequestBody DepositRequestDto request) {

        return accountService.deposit(login, request.getAmount());
    }

    @PostMapping("/{login}/withdraw")
    //@PreAuthorize ??
    public AccountResponseDto withdraw(
            @PathVariable String login,
            @RequestBody WithdrawRequestDto request) {

        return accountService.withdraw(login, request.getAmount());
    }
}
