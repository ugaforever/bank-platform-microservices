package ru.ugaforever.bank.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ugaforever.bank.account.dto.AccountRequestDto;
import ru.ugaforever.bank.account.dto.AccountResponseDto;
import ru.ugaforever.bank.account.service.AccountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AccountController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class }
)
public class AccountControllerTest {

    private static final String BASE_URL = "/account";

    private static final Long ACCOUNT_ID = 1L;
    private static final String LOGIN = "ivanov";
    private static final String NAME = "Иванов Иван";
    private static final LocalDate BIRTHDATE = LocalDate.of(2001, 1, 1);
    private static final BigDecimal BALANCE = BigDecimal.valueOf(100);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService service;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountRequestDto validRequest;
    private AccountResponseDto validResponse;

    @BeforeEach
    void setUp() {
        validRequest = new AccountRequestDto(LOGIN, NAME, BIRTHDATE, BALANCE);
        validResponse = new AccountResponseDto(ACCOUNT_ID, LOGIN, NAME, BIRTHDATE, BALANCE);
    }

    @Test
    @DisplayName("GET /account/{id} — должен вернуть пользователя")
    void shouldGetAccountById() throws Exception {
        when(service.getAccount(ACCOUNT_ID)).thenReturn(validResponse);

        mockMvc.perform(get(BASE_URL + "/" + ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.login").value(LOGIN))
                .andExpect(jsonPath("$.name").value(NAME))
                .andExpect(jsonPath("$.birthdate").value(BIRTHDATE.format(FORMATTER)))
                .andExpect(jsonPath("$.balance").value(BALANCE.doubleValue()));
    }

    @Test
    @DisplayName("GET /account/{id} — должен вернуть 404 если не найден")
    void shouldReturn404WhenAccountNotFound() throws Exception {
        long missingId = 999L;
        when(service.getAccount(missingId))
                .thenThrow(new IllegalArgumentException("Account with ID " + missingId + " not found"));

        mockMvc.perform(get(BASE_URL + "/" + missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account with ID 999 not found"));
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
