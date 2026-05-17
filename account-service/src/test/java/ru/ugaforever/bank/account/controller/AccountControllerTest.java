package ru.ugaforever.bank.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ugaforever.bank.account.dto.AccountRequestDto;
import ru.ugaforever.bank.account.dto.AccountResponseDto;
import ru.ugaforever.bank.account.service.AccountService;

import java.math.BigDecimal;
import java.time.LocalDate;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    private static final String BASE_URL = "/account";

    private static final Long ACCOUNT_ID = 1L;
    private static final String LOGIN = "ivanov";
    private static final String NAME = "Иванов Иван";
    private static final LocalDate BIRTHDATE = LocalDate.of(2001, 1, 1);
    private static final BigDecimal BALANCE = BigDecimal.valueOf(100);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService service;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountRequestDto validRequest;
    private AccountResponseDto validResponse;

}
