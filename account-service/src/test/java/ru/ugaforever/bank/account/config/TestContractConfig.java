package ru.ugaforever.bank.account.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import ru.ugaforever.bank.chassis.client.NotificationClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import static org.mockito.Mockito.mock;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        org.springframework.cloud.openfeign.FeignAutoConfiguration.class
})
public class TestContractConfig {

    @Bean
    @Primary
    public NotificationClient notificationClient() {
        return mock(NotificationClient.class);
    }
}
