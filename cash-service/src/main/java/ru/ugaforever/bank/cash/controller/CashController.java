package ru.ugaforever.bank.cash.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;

import ru.ugaforever.bank.cash.service.CashService;
import ru.ugaforever.bank.chassis.dto.cash.DepositRequestDto;
import ru.ugaforever.bank.chassis.dto.cash.WithdrawRequestDto;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cash")
public class CashController {

    private final CashService cashService;

    //curl -v -X POST http://localhost:9004/cash/deposit -H "Content-Type: application/json" -d '{"accountId": 1, "amount": 500.00}'
    @PostMapping("/deposit")
    //@PreAuthorize ??
    public ResponseEntity<CashResponseDto> deposit(@Valid @RequestBody DepositRequestDto request) {
        CashResponseDto response = cashService.deposit(request);
        return ResponseEntity.ok(response);
    }

    //curl -v -X POST http://localhost:9004/cash/withdraw -H "Content-Type: application/json" -d '{"accountId": 1, "amount": 100.00}'
    @PostMapping("/withdraw")
    //@PreAuthorize ??
    public ResponseEntity<CashResponseDto> withdraw(@Valid @RequestBody WithdrawRequestDto request) {
        CashResponseDto response = cashService.withdraw(request);
        return ResponseEntity.ok(response);
    }
}
