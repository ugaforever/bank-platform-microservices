package ru.ugaforever.bank.frontui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferRequestDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferResponseDto;
import ru.ugaforever.bank.frontui.service.AccountService;
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
    public String transfer(
            Model model,
            @RequestParam("value") int value,
            @RequestParam("login") String login
    ) {
        //String login = principal.getPreferredUsername();

        TransferRequestDto requestDto = TransferRequestDto.builder()
                .fromLogin("ivanov")
                .toLogin(login)
                .amount(BigDecimal.valueOf(value))
                .build();

        transferService.submit(requestDto);

        return "redirect:/";
    }

}
