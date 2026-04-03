package com.guimarobo.Fintrack.worker;

import com.guimarobo.Fintrack.model.Transaction;
import com.guimarobo.Fintrack.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionCategorizationWorker {

    private final TransactionRepository transactionRepository;

    public TransactionCategorizationWorker(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Async("categorizationPool")
    @Transactional
    public void categorizar(Transaction transaction) {
        try {
            String desc = transaction.getDescription().toLowerCase();
            String categoria;

            if (desc.contains("mercado") || desc.contains("supermercado") || desc.contains("padaria")) {
                categoria = "ALIMENTACAO";
            } else if (desc.contains("uber") || desc.contains("99") || desc.contains("onibus")) {
                categoria = "TRANSPORTE";
            } else if (desc.contains("aluguel") || desc.contains("condominio") || desc.contains("luz")) {
                categoria = "MORADIA";
            } else if (desc.contains("cinema") || desc.contains("netflix") || desc.contains("spotify")) {
                categoria = "LAZER";
            } else {
                categoria = "OUTROS";
            }

            transactionRepository.updateCategory(transaction.getId(), categoria);

            log.info("Transação ID: {} categorizada como {} (descrição: \"{}\")",
                    transaction.getId(), categoria, transaction.getDescription());

        } catch (Exception e) {
            log.error("Falha ao categorizar transação ID: {} — erro: {}",
                    transaction.getId(), e.getMessage());
        }
    }
}
