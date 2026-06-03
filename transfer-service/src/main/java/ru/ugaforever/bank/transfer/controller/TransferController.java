package ru.ugaforever.bank.transfer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.chassis.dto.transfer.TransferRequestDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferResponseDto;
import ru.ugaforever.bank.transfer.service.TransferService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transfer")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public TransferResponseDto submit(@Valid @RequestBody TransferRequestDto request) {

        return transferService.submit(request);
    }
}
