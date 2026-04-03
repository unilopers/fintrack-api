package com.guimarobo.Fintrack.dto;

import java.math.BigDecimal;

public class TransactionReportResponse {

    private int mes;
    private int ano;
    private BigDecimal totalEntradas;
    private BigDecimal totalDespesas;
    private BigDecimal saldo;
    private int totalTransacoes;

    public TransactionReportResponse() {}

    public TransactionReportResponse(int mes, int ano, BigDecimal totalEntradas,
                                     BigDecimal totalDespesas, BigDecimal saldo, int totalTransacoes) {
        this.mes = mes;
        this.ano = ano;
        this.totalEntradas = totalEntradas;
        this.totalDespesas = totalDespesas;
        this.saldo = saldo;
        this.totalTransacoes = totalTransacoes;
    }

    public int getMes() {
        return mes;
    }

    public int getAno() {
        return ano;
    }

    public BigDecimal getTotalEntradas() {
        return totalEntradas;
    }

    public BigDecimal getTotalDespesas() {
        return totalDespesas;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public int getTotalTransacoes() {
        return totalTransacoes;
    }
}
