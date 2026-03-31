package com.guimarobo.Fintrack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "transactions") // criacao table db
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto incremento
    private Long id;

    @NotBlank(message = "A descrição é obrigatória.")
    private String description;

    @NotNull(message = "O valor (amount) é obrigatório.")
    @jakarta.validation.constraints.DecimalMin(value = "0.01", message = "O valor da transação deve ser maior que zero.")
    private BigDecimal amount;

    @NotBlank(message = "O tipo da transação é obrigatório (DESPESA ou ENTRADA).")
    private String type; // ENTRADA ou DESPESA

    @NotNull(message = "A data é obrigatória.")
    private LocalDate date;

    private String category; // ALIMENTACAO, TRANSPORTE, LAZER, MORADIA, OUTROS

    @NotNull(message = "A conta relacionada (account) é obrigatória.")
    @ManyToOne // relacionamento muitos pra um
    @JoinColumn(name = "account_id") // chave estrangeira
    @JsonIgnoreProperties({"transactions"}) // evitar loop
    private Account account;

    public Transaction() {}

    public Transaction(Long id, String description, BigDecimal amount, String type, LocalDate date, Account account) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", date=" + date +
                '}';
    }
}
