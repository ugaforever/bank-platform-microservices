package ru.ugaforever.bank.chassis.exception;

public class AccountNotFoundException extends ResourceNotFoundException {
    public AccountNotFoundException(String login) {
        super("Account", login);
    }
}
