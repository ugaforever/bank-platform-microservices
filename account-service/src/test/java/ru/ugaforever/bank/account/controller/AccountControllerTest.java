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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ugaforever.bank.account.service.AccountService;
import ru.ugaforever.bank.chassis.dto.account.AccountRequestDto;
import ru.ugaforever.bank.chassis.dto.account.AccountResponseDto;
import ru.ugaforever.bank.chassis.dto.account.AccountUpdateDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AccountController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class }
)
@ActiveProfiles("test")
public class AccountControllerTest {

    private static final String BASE_URL = "/account";

    private static final Long ACCOUNT_ID = 1L;
    private static final String LOGIN = "ivanov";
    private static final String NAME = "Иванов Иван";
    private static final LocalDate BIRTHDATE = LocalDate.of(2001, 1, 1);
    private static final BigDecimal BALANCE = BigDecimal.valueOf(100);

    private static final String UPDATE_NAME = "Иванов Сергей";
    private static final LocalDate UPDATE_BIRTHDATE = LocalDate.of(2003, 3, 3);

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
    @DisplayName("GET /account/{login} — должен вернуть пользователя")
    void shouldGetAccountByLogin() throws Exception {
        when(service.getAccount(LOGIN)).thenReturn(validResponse);

        mockMvc.perform(get(BASE_URL + "/" + LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.login").value(LOGIN))
                .andExpect(jsonPath("$.name").value(NAME))
                .andExpect(jsonPath("$.birthdate").value(BIRTHDATE.format(FORMATTER)))
                .andExpect(jsonPath("$.balance").value(BALANCE.doubleValue()));
    }

    @Test
    @DisplayName("GET /account/{login} — должен вернуть 404 если не найден")
    void shouldReturn404WhenAccountNotFound() throws Exception {
        String missingLogin = "missingLogin";
        when(service.getAccount(missingLogin))
                .thenThrow(new IllegalArgumentException("Account with ID " + missingLogin + " not found"));

        mockMvc.perform(get(BASE_URL + "/" + missingLogin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account with ID " + missingLogin + " not found"));
    }

    @Test
    @DisplayName("PATCH /account/{login} — должен обновить несколько полей и вернуть 200")
    void shouldUpdateMultipleFields() throws Exception {
        // given
        AccountUpdateDto updateDto = AccountUpdateDto.builder()
                .name(UPDATE_NAME)
                .birthdate(UPDATE_BIRTHDATE)
                .build();

        AccountResponseDto responseDto = AccountResponseDto.builder()
                .id(ACCOUNT_ID)
                .login(LOGIN)
                .name(UPDATE_NAME)
                .birthdate(UPDATE_BIRTHDATE)
                .balance(BALANCE)
                .build();

        when(service.updateAccount(eq(LOGIN), any(AccountUpdateDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(patch(BASE_URL + "/" + LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.login").value(LOGIN))
                .andExpect(jsonPath("$.name").value(UPDATE_NAME))
                .andExpect(jsonPath("$.birthdate").value(UPDATE_BIRTHDATE.format(FORMATTER)))
                .andExpect(jsonPath("$.balance").value(BALANCE.doubleValue()));
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
