package com.guimarobo.Fintrack.worker;

import com.guimarobo.Fintrack.model.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class LowBalanceAlertWorker {

    private static final BigDecimal THRESHOLD = new BigDecimal("100.00");

    @Async("fintrackAsyncPool")
    public void verificarSaldo(Account account) {
        try {
            BigDecimal saldo = account.getBalance();

            if (saldo.compareTo(THRESHOLD) < 0) {
                log.warn("ALERTA: Conta '{}' (ID: {}) com saldo baixo: R$ {} (limite: R$ {})",
                        account.getBankName(),
                        account.getId(),
                        saldo,
                        THRESHOLD);
            } else {
                log.info("Conta '{}' (ID: {}) com saldo saudável: R$ {}",
                        account.getBankName(),
                        account.getId(),
                        saldo);
            }
        } catch (Exception e) {
            log.error("Erro ao verificar saldo da conta ID: {} — {}",
                    account.getId(), e.getMessage());
        }
    }
}
