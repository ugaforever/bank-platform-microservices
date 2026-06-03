package ru.ugaforever.bank.chassis.exception;

public class DuplicateUsernameException extends ConflictException {
    public DuplicateUsernameException(String username) {
        super("Username '" + username + "' already exists");
    }
}
