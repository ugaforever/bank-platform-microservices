package ru.ugaforever.bank.frontui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ugaforever.bank.chassis.dto.cash.CashAction;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.frontui.service.CashService;

@Slf4j
@Controller
@RequestMapping("/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;
    /**
     * POST /cash - пополнение \ снятие денег.
     *
     * 1. value - сумма
     * 2. action - WITHDRAW (снять), DEPOSIT (пополнить)
     */
    @PostMapping
    public String editCash(
            Model model,
            @RequestParam("value") int value,
            @RequestParam("action") CashAction action
            //@AuthenticationPrincipal OidcUser principal
    ) {
        String login = "ivanov";
        //String login = principal.getPreferredUsername();
        log.info("Cash operation: login={}, action={}, value={}", login, action, value);

        CashResponseDto cash = switch (action) {
            case WITHDRAW -> cashService.withdraw(login, value);
            case DEPOSIT -> cashService.deposit(login, value);
        };

        //model.addAttribute("balance", cash.getAmount());
        //return "main";

        return "redirect:/";
    }
}
