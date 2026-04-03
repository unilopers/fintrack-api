package com.guimarobo.Fintrack.dto;

import java.math.BigDecimal;

public class AccountResponse {

    private Long id;
    private String bankName;
    private String accountType;
    private BigDecimal balance;

    public AccountResponse() {}

    public AccountResponse(Long id, String bankName, String accountType, BigDecimal balance) {
        this.id = id;
        this.bankName = bankName;
        this.accountType = accountType;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
