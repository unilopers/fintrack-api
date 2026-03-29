package com.guimarobo.Fintrack.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class AuditWorker {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Async("auditPool")
    public void registrarAuditoria(Long userId, String operacao, String detalhe) {
        log.info("[AUDIT] iniciando registro — userId={} | operacao={}", userId, operacao);

        try {
            // simula uma latência real (ex: gravar em arquivo, chamar serviço externo)
            Thread.sleep(500);

            String timestamp = LocalDateTime.now().format(FMT);

            log.info("[AUDIT] ✓ userId={} | operacao={} | detalhe={} | timestamp={}",
                    userId, operacao, detalhe, timestamp);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[AUDIT] ✗ Falha ao registrar auditoria — userId={} | operacao={} | erro={}",
                    userId, operacao, e.getMessage());
        }
    }
}