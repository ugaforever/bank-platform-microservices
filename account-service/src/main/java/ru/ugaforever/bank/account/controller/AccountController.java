package ru.ugaforever.bank.account.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ugaforever.bank.account.model.AccountOwnerResponse;
import ru.ugaforever.bank.account.model.TransferRequest;
import ru.ugaforever.bank.account.service.AccountService;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('SERVICE') && hasAuthority('accounts.write')")
    public String transfer(@RequestBody TransferRequest request) {
        return "Перевод выполнен: "
                + request.getAmount()
                + " со счёта " + request.getFromAccountId()
                + " на счёт " + request.getToAccountId();
    }

    @GetMapping("/{accountId}/owner")
    @PreAuthorize("hasRole('SERVICE')")
    public AccountOwnerResponse getOwner(@PathVariable String accountId) {
        var account = accountService.getAccount(accountId);
        return new AccountOwnerResponse(account.getId(), account.getOwnerUsername());
    }
}
