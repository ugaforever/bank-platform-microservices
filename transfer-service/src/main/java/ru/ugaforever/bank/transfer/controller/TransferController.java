package ru.ugaforever.bank.transfer.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.transfer.dto.TransferRequestDto;
import ru.ugaforever.bank.transfer.service.TransferService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transfer")
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);

    private final TransferService transferService;

    //curl -v -X GET http://localhost:9003/transfer/preview
    @GetMapping("/preview")
    //@PreAuthorize("hasRole('USER')")
    public String preview() {
        return transferService.preview();
    }

    @PostMapping
    //@PreAuthorize("hasRole('USER') && hasAuthority('transfer.write')")
    public String submit(
            @RequestBody TransferRequestDto request,
            JwtAuthenticationToken authentication
    ) {
        log.info("Submit called by {} with authorities {}", authentication.getName(), authentication.getAuthorities());
        return transferService.submit(request, authentication);
    }
}
