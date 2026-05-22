package ru.ugaforever.bank.notification.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationResponseDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;
import ru.ugaforever.bank.notification.service.NotificationService;

import java.time.Instant;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("contract-test")
public abstract class NotificationContractTest {

    private static final String MESSAGE = "Your operation was successful";
    private static final String DATETIME = "2026-01-01T01:02:03.456Z";

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected NotificationService notificationService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        NotificationRequestDto request = NotificationRequestDto.builder()
                .source(NotificationSource.ACCOUNT_SERVICE)
                .message(MESSAGE)
                .build();

        NotificationResponseDto response = NotificationResponseDto.builder()
                .id(1L)
                .source(NotificationSource.ACCOUNT_SERVICE)
                .message(MESSAGE)
                .actionAt(Instant.parse(DATETIME))
                .build();

        when(notificationService.notification(request))
                .thenReturn(response);
    }
}
