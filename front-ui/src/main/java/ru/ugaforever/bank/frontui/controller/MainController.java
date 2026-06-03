package ru.ugaforever.bank.frontui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    /**
     * GET / — редирект на GET /account
     */
    @GetMapping
    public String index(Model model) {

        return "redirect:/account";
    }
}
