package ru.ugaforever.bank.transfer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.transfer.model.TransferRequest;
import ru.ugaforever.bank.transfer.service.TransferService;

@RestController
@RequestMapping("/transfer")
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping("/preview")
    @PreAuthorize("hasRole('USER')")
    public String preview() {
        return transferService.preview();
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') && hasAuthority('transfer.write')")
    public String submit(
            @RequestBody TransferRequest request,
            JwtAuthenticationToken authentication
    ) {
        log.info("Submit called by {} with authorities {}", authentication.getName(), authentication.getAuthorities());
        return transferService.submit(request, authentication);
    }
}
