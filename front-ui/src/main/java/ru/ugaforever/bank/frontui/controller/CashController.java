package ru.ugaforever.bank.frontui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    @PreAuthorize("hasRole('USER') && hasAuthority('cash.write')")
    public String editCash(
            Model model,
            @RequestParam("value") int value,
            @RequestParam("action") CashAction action,
            @AuthenticationPrincipal OAuth2User oAuth2User
    ) {

        if (oAuth2User == null) {
            model.addAttribute("authenticated", false);
            return "redirect:/";
        }

        String login = (String) oAuth2User.getAttributes().get("preferred_username");
        log.info("Cash operation: login={}, action={}, value={}", login, action, value);

        switch (action) {
            case WITHDRAW -> cashService.withdraw(login, value);
            case DEPOSIT -> cashService.deposit(login, value);
        };

        return "redirect:/";
    }
}
