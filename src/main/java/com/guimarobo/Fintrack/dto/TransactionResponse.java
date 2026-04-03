package com.guimarobo.Fintrack.dto;

import com.guimarobo.Fintrack.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDate date;
    private String category;
    private Long accountId;
    private String accountBankName;

    public TransactionResponse() {}

    public TransactionResponse(Long id, String description, BigDecimal amount, TransactionType type,
                               LocalDate date, String category, Long accountId, String accountBankName) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.category = category;
        this.accountId = accountId;
        this.accountBankName = accountBankName;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccountBankName() {
        return accountBankName;
    }
}
