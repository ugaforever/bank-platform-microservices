package ru.ugaforever.bank.account.model;

import java.math.BigDecimal;

public class Account {

    private String id;
    private String ownerUsername;
    private BigDecimal balance;

    public Account() {
    }

    public Account(String id, String ownerUsername, BigDecimal balance) {
        this.id = id;
        this.ownerUsername = ownerUsername;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
