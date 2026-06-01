package ru.ugaforever.bank.cash.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;

import ru.ugaforever.bank.cash.service.CashService;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cash")
public class CashController {

    private final CashService cashService;

    @PostMapping("/deposit")
    @PreAuthorize("isAuthenticated()")
    public CashResponseDto deposit(@Valid @RequestBody DepositRequestDto request) {
        return cashService.deposit(request);
    }

    @PostMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    public CashResponseDto withdraw(@Valid @RequestBody WithdrawRequestDto request) {
        return cashService.withdraw(request);
    }
}
