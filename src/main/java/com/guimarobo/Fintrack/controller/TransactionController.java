package com.guimarobo.Fintrack.controller;

import com.guimarobo.Fintrack.dto.TransactionReportResponse;
import com.guimarobo.Fintrack.dto.TransactionResponse;
import com.guimarobo.Fintrack.model.Transaction;
import com.guimarobo.Fintrack.model.User;
import com.guimarobo.Fintrack.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody Transaction transaction,
                                                                 @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(transactionService.save(transaction, user)));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(@AuthenticationPrincipal User user) {
        List<TransactionResponse> responses = transactionService.findAll(user).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id,
                                                                  @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(toResponse(transactionService.findById(id, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id,
                                                                 @RequestBody Transaction updatedTransaction,
                                                                 @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(toResponse(transactionService.update(id, updatedTransaction, user)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TransactionResponse> patchTransaction(@PathVariable Long id,
                                                                @RequestBody Map<String, String> fields,
                                                                @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(toResponse(transactionService.patch(id, fields, user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id,
                                                  @AuthenticationPrincipal User user) {
        transactionService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/report")
    public ResponseEntity<TransactionReportResponse> generateReport(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {

        LocalDate hoje = LocalDate.now();
        int mesFinal = (mes != null) ? mes : hoje.getMonthValue();
        int anoFinal = (ano != null) ? ano : hoje.getYear();

        TransactionReportResponse report = transactionService.generateReport(user, mesFinal, anoFinal);
        return ResponseEntity.ok(report);
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getDescription(),
                t.getAmount(),
                t.getType(),
                t.getDate(),
                t.getCategory(),
                t.getAccount().getId(),
                t.getAccount().getBankName()
        );
    }
}
