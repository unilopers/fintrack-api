package com.guimarobo.Fintrack.async.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditLogWorker {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogWorker.class);

    @Async("auditPool")
    public void registrarAuditoria(String nomeUsuario, String acao, LocalDateTime timestamp) {
        logger.info("[AUDIT] Iniciando registro — Usuário: {} | Ação: {} | Data: {}",
                nomeUsuario, acao, timestamp);

        try {
            Thread.sleep(1000);

            logger.info("Auditoria registrada - Usuário: {}, Ação: {}, Data: {}",
                    nomeUsuario, acao, timestamp);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[AUDIT] Falha ao registrar auditoria — Usuário: {} | Ação: {} | Erro: {}",
                    nomeUsuario, acao, e.getMessage());
        } catch (Exception e) {
            logger.error("[AUDIT] Erro inesperado ao registrar auditoria — Usuário: {} | Ação: {} | Erro: {}",
                    nomeUsuario, acao, e.getMessage());
        }
    }
}
