package ru.ugaforever.bank.chassis.exception;

public class AccountNotFoundException extends ResourceNotFoundException {
    public AccountNotFoundException(Long id) {
        super("Account", id.toString());
    }
}
