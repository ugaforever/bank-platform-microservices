package ru.ugaforever.bank.frontui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;
import ru.ugaforever.bank.chassis.dto.cash.CashResponseDto;
import ru.ugaforever.bank.chassis.dto.cash.CashAction;
import ru.ugaforever.bank.frontui.controller.stub.AccountStub;
import ru.ugaforever.bank.frontui.service.AccountService;
//import ru.ugaforever.bank.frontui.service.CashService;


import java.time.LocalDate;

/**
 * Контроллер main.html.
 *
 * Используемая модель для main.html:
 *      model.addAttribute("name", name);
 *      model.addAttribute("birthdate", birthdate.format(DateTimeFormatter.ISO_DATE));
 *      model.addAttribute("sum", sum);
 *      model.addAttribute("accounts", accounts);
 *      model.addAttribute("errors", errors);
 *      model.addAttribute("info", info);
 *
 * Поля модели:
 *      name - Фамилия Имя текущего пользователя, String (обязательное)
 *      birthdate - дата рождения текущего пользователя, String в формате 'YYYY-MM-DD' (обязательное)
 *      sum - сумма на счету текущего пользователя, Integer (обязательное)
 *      accounts - список аккаунтов, которым можно перевести деньги, List<AccountDto> (обязательное)
 *      errors - список ошибок после выполнения действий, List<String> (не обязательное)
 *      info - строка успешности после выполнения действия, String (не обязательное)
 *
 * С примерами использования можно ознакомиться в тестовом классе заглушке AccountStub
 */
@Controller
@RequiredArgsConstructor
public class MainController {

    /**
     * GET / — редирект на GET /account
     */
    @GetMapping
    public String index() {
        return "redirect:/account";
    }

    // TODO: Удалить заглушку, так как используется только для ознакомительных целей
    @Autowired
    private AccountStub accountStub;

    /*private void fillModel(Model model, @Nullable List<String> errors, @Nullable String info) {
        model.addAttribute("name", name);
        model.addAttribute("birthdate", birthdate.format(DateTimeFormatter.ISO_DATE));
        model.addAttribute("sum", sum);
        model.addAttribute("accounts", accounts);
        model.addAttribute("errors", errors);
        model.addAttribute("info", info);
    }*/
}
