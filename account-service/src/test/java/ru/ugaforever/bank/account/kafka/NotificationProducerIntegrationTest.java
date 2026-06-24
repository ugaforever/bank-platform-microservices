package ru.ugaforever.bank.account.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.ugaforever.bank.account.config.TestKafkaConfig;
import ru.ugaforever.bank.account.model.Account;
import ru.ugaforever.bank.account.repository.AccountRepository;
import ru.ugaforever.bank.account.service.AccountService;
import ru.ugaforever.bank.chassis.dto.account.AccountRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationRequestDto;
import ru.ugaforever.bank.chassis.dto.notification.NotificationSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EmbeddedKafka(
        topics = {"bank.notification", "bank.notification.dlt"},
        partitions = 1,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:0",
                "auto.create.topics.enable=true"
        }
)
@ActiveProfiles("test")
@Import(TestKafkaConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NotificationProducerIntegrationTest {

        @Autowired
        private KafkaTemplate<String, String> kafkaTemplate;

        @Autowired
        private AccountService accountService;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AccountRepository accountRepository;

        @Autowired
        private KafkaConsumer<String, String> consumer;

        @Test
        void shouldSendNotification() throws Exception {
                // Given
                AccountRequestDto accountDto = AccountRequestDto.builder()
                        .login("LOGIN")
                        .build();

                Account savedAccount = Account.builder()
                        .id(1L)
                        .login(accountDto.getLogin())
                        .build();

                when(accountRepository.save(any(Account.class)))
                        .thenReturn(savedAccount);

                // When
                accountService.createAccount(accountDto);

                // Then
                ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
                        consumer,
                        "bank.notification"
                );

                assertThat(record).isNotNull();

                NotificationRequestDto notification = objectMapper.readValue(
                        record.value(),
                        NotificationRequestDto.class
                );

                assertThat(notification.getSource()).isEqualTo(NotificationSource.ACCOUNT_SERVICE);
                assertThat(notification.getMessage()).contains("LOGIN");
        }
}