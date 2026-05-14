package ru.ugaforever.bank.account.model;

public class AccountOwnerResponse {

    private String accountId;
    private String ownerUsername;

    public AccountOwnerResponse() {
    }

    public AccountOwnerResponse(String accountId, String ownerUsername) {
        this.accountId = accountId;
        this.ownerUsername = ownerUsername;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
}
