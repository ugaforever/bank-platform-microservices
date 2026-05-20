package ru.ugaforever.bank.transfer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.chassis.dto.transfer.TransferRequestDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferResponseDto;
import ru.ugaforever.bank.transfer.service.TransferService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transfer")
public class TransferController {

    private final TransferService transferService;

    //curl -v -X GET http://localhost:9003/transfer/preview
    @GetMapping("/preview")
    //@PreAuthorize("hasRole('USER')")
    public String preview() {
        return transferService.preview();
    }

    @PostMapping
    //@PreAuthorize("hasRole('USER') && hasAuthority('transfer.write')")
    public TransferResponseDto submit(
            @Valid @RequestBody TransferRequestDto request
            //JwtAuthenticationToken authentication
    ) {

        return transferService.submit(request/*, authentication*/);
    }
}
