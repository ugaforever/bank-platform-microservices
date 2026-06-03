package ru.ugaforever.bank.frontui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ugaforever.bank.chassis.dto.transfer.TransferRequestDto;
import ru.ugaforever.bank.frontui.service.TransferService;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * POST /transfer.
     * Что нужно сделать:
     * 1. Сходить в сервис accounts через Gateway API для перевода со счета текущего аккаунта на счет другого аккаунта по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     *
     * Параметры:
     * 1. value - сумма списания
     * 2. login - логин пользователя получателя
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER') && hasAuthority('transfer.write')")
    public String transfer(
            Model model,
            @RequestParam("value") int value,
            @RequestParam("login") String toLogin,
            @AuthenticationPrincipal OAuth2User oAuth2User
    ) {
        if (oAuth2User == null) {
            model.addAttribute("authenticated", false);
            return "redirect:/";
        }

        String fromLogin = (String) oAuth2User.getAttributes().get("preferred_username");

        TransferRequestDto requestDto = TransferRequestDto.builder()
                .fromLogin(fromLogin)
                .toLogin(toLogin)
                .amount(BigDecimal.valueOf(value))
                .build();

        transferService.submit(requestDto);

        return "redirect:/";
    }
}
