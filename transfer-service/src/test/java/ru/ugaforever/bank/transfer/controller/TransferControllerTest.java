package ru.ugaforever.bank.transfer.controller;

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
import ru.ugaforever.bank.chassis.dto.transfer.TransferRequestDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferResponseDto;
import ru.ugaforever.bank.chassis.dto.transfer.TransferStatus;
import ru.ugaforever.bank.transfer.service.TransferService;

import java.math.BigDecimal;


import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(
        value = TransferController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class }
)
@ActiveProfiles("test")
public class TransferControllerTest {

    private static final String BASE_URL = "/transfer";

    private static final String FROM_LOGIN = "ivanov";
    private static final String TO_LOGIN = "petrov";
    private static final BigDecimal AMOUNT = new BigDecimal("100.50");
    private static final Long TRANSFER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransferRequestDto validRequest;
    private TransferResponseDto validResponse;

    @BeforeEach
    void setUp() {
        validRequest = TransferRequestDto.builder()
                .fromLogin(FROM_LOGIN)
                .toLogin(TO_LOGIN)
                .amount(AMOUNT)
                .build();

        validResponse = TransferResponseDto.builder()
                .id(TRANSFER_ID)
                .fromLogin(FROM_LOGIN)
                .toLogin(TO_LOGIN)
                .amount(AMOUNT)
                .status(TransferStatus.TRANSFER_PENDING)
                .build();
    }

    @Test
    @DisplayName("POST /transfer — должен успешно создать перевод")
    void shouldSubmitTransferSuccessfully() throws Exception {
        when(transferService.submit(any(TransferRequestDto.class))).thenReturn(validResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TRANSFER_ID))
                .andExpect(jsonPath("$.fromLogin").value(FROM_LOGIN))
                .andExpect(jsonPath("$.toLogin").value(TO_LOGIN))
                .andExpect(jsonPath("$.amount").value(AMOUNT.doubleValue()))
                .andExpect(jsonPath("$.status").value(TransferStatus.TRANSFER_PENDING.name()));
    }

    @Test
    @DisplayName("POST /transfer — должен вернуть 400 при невалидном запросе")
    void shouldReturn400WhenInvalidRequest() throws Exception {
        TransferRequestDto invalidRequest = TransferRequestDto.builder()
                .fromLogin("") // пустой логин
                .toLogin(TO_LOGIN)
                .amount(AMOUNT)
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /transfer — должен вернуть 400 при отрицательной сумме")
    void shouldReturn400WhenNegativeAmount() throws Exception {
        TransferRequestDto invalidRequest = TransferRequestDto.builder()
                .fromLogin(FROM_LOGIN)
                .toLogin(TO_LOGIN)
                .amount(new BigDecimal("-50.00"))
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /transfer — должен вернуть 400 при нулевой сумме")
    void shouldReturn400WhenZeroAmount() throws Exception {
        TransferRequestDto invalidRequest = TransferRequestDto.builder()
                .fromLogin(FROM_LOGIN)
                .toLogin(TO_LOGIN)
                .amount(BigDecimal.ZERO)
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /transfer — должен вернуть 400 если fromLogin отсутствует")
    void shouldReturn400WhenFromLoginIsMissing() throws Exception {
        TransferRequestDto invalidRequest = TransferRequestDto.builder()
                .toLogin(TO_LOGIN)
                .amount(AMOUNT)
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /transfer — должен вернуть 400 если toLogin отсутствует")
    void shouldReturn400WhenToLoginIsMissing() throws Exception {
        TransferRequestDto invalidRequest = TransferRequestDto.builder()
                .fromLogin(FROM_LOGIN)
                .amount(AMOUNT)
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /transfer — должен вернуть 404 если аккаунт не найден")
    void shouldReturn404WhenAccountNotFound() throws Exception {
        when(transferService.submit(any(TransferRequestDto.class)))
                .thenThrow(new IllegalArgumentException("Account not found: " + FROM_LOGIN));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /transfer — должен обработать перевод с минимальной суммой")
    void shouldHandleMinimumAmount() throws Exception {
        TransferRequestDto minAmountRequest = TransferRequestDto.builder()
                .fromLogin(FROM_LOGIN)
                .toLogin(TO_LOGIN)
                .amount(new BigDecimal("0.01"))
                .build();

        TransferResponseDto minAmountResponse = TransferResponseDto.builder()
                .id(2L)
                .fromLogin(FROM_LOGIN)
                .toLogin(TO_LOGIN)
                .amount(new BigDecimal("0.01"))
                .status(TransferStatus.TRANSFER_PENDING)
                .build();

        when(transferService.submit(any(TransferRequestDto.class))).thenReturn(minAmountResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(minAmountRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(0.01));
    }

    @Test
    @DisplayName("POST /transfer — должен обработать перевод с большой суммой")
    void shouldHandleLargeAmount() throws Exception {
        BigDecimal largeAmount = new BigDecimal("999999.99");
        TransferRequestDto largeAmountRequest = TransferRequestDto.builder()
                .fromLogin(FROM_LOGIN)
                .toLogin(TO_LOGIN)
                .amount(largeAmount)
                .build();

        TransferResponseDto largeAmountResponse = TransferResponseDto.builder()
                .id(3L)
                .fromLogin(FROM_LOGIN)
                .toLogin(TO_LOGIN)
                .amount(largeAmount)
                .status(TransferStatus.TRANSFER_PENDING)
                .build();

        when(transferService.submit(any(TransferRequestDto.class))).thenReturn(largeAmountResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(largeAmountRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(999999.99));
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}