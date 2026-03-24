package com.guimarobo.Fintrack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto incremento
    private Long id;

    @NotBlank(message = "O nome do banco é obrigatório")
    private String bankName;

    @NotBlank(message = "O tipo da conta é obrigatório")
    private String accountType; // Corrente, poupança....

    @NotNull(message = "O saldo inicial é obrigatório")
    private BigDecimal balance;

    @ManyToOne // relacionamento muitos pra um
    @JoinColumn(name = "user_id", nullable = false) // chave estrangeira, nao pode ser null
    @JsonIgnoreProperties("accounts") // evitar loop
    private User user;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("account")
    private List<Transaction> transactions;

    public Account() {}

    public Account(String bankName, String accountType, BigDecimal balance, User user) {
        this.bankName = bankName;
        this.accountType = accountType;
        this.balance = balance;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", bankName='" + bankName + '\'' +
                ", accountType='" + accountType + '\'' +
                ", balance=" + balance +
                '}';
    }
}
