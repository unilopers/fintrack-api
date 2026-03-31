package com.guimarobo.Fintrack.worker;

import com.guimarobo.Fintrack.model.Transaction;
import com.guimarobo.Fintrack.model.User;
import com.guimarobo.Fintrack.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class TransactionReportWorker {

    private final TransactionRepository transactionRepository;

    public TransactionReportWorker(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Async("fintrackAsyncPool")
    public void generate(User user, int mes, int ano) {
        log.info("Gerando relatório para usuário ID: {}...", user.getId());

        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        for (int tentativa = 1; tentativa <= 3; tentativa++) {
            try {
                processarRelatorio(user, inicio, fim);
                break;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread interrompida durante geração do relatório — userId={}", user.getId());
                return;

            } catch (Exception e) {
                log.warn("Tentativa {}/3 falhou: {}", tentativa, e.getMessage());
                if (tentativa == 3) {
                    log.error("Relatório falhou após 3 tentativas para usuário ID: {}", user.getId());
                }
            }
        }
    }

    private void processarRelatorio(User user, LocalDate inicio, LocalDate fim)
            throws InterruptedException {

        Thread.sleep(3000); // simula processamento pesado

        List<Transaction> transacoes = transactionRepository
                .findByAccountUserAndDateBetween(user, inicio, fim);


        BigDecimal totalEntradas = transacoes.stream()
                .filter(t -> "ENTRADA".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDespesas = transacoes.stream()
                .filter(t -> "DESPESA".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldo = totalEntradas.subtract(totalDespesas);

        log.info("Relatório concluído - Entradas: R$ {} | Despesas: R$ {} | Saldo: R$ {}",
                totalEntradas, totalDespesas, saldo);
    }
}