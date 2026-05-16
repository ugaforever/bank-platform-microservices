package ru.ugaforever.bank.frontui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
public class FrontUIApplication {
    public static void main(String[] args) {
        SpringApplication.run(FrontUIApplication.class, args);
    }
}

