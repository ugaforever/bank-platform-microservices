package ru.ugaforever.bank.frontui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;
import ru.ugaforever.bank.frontui.service.AccountService;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;



    /**
     * GET /account — получение данных текущего пользователя
     */
    @GetMapping("/account")
    public String getAccount(
            Model model
            //@AuthenticationPrincipal OidcUser principal
    ) {
        //String login = principal.getPreferredUsername();

        AccountResponseDto account = accountService.getAccount(1L);

        model.addAttribute("login", account.getLogin());
        model.addAttribute("name", account.getName());
        model.addAttribute("birthdate", account.getBirthdate());
        model.addAttribute("balance", account.getBalance());

        return "main";
    }

    /**
     * POST /account - изменение данных
     * 1. name - Фамилия Имя
     * 2. birthdate - дата рождения в формате YYYY-DD-MM
     */
    @PostMapping("/account")
    public String editAccount(
            Model model,
            @RequestParam("name") String name,
            @RequestParam("birthdate") LocalDate birthdate
    ) {
        // TODO: 1L заменить на login (nекущего пользователя можно получить из контекста Security)

        AccountUpdateDto update = AccountUpdateDto.builder()
                .name(name)
                .birthdate(birthdate)
                .build();

        AccountResponseDto account = accountService.patchAccount(1L, update);

        model.addAttribute("login", account.getLogin());
        model.addAttribute("name", account.getName());
        model.addAttribute("birthdate", account.getBirthdate());
        model.addAttribute("balance", account.getBalance());

        return "main";
    }


}
