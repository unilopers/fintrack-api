package com.guimarobo.Fintrack.controller;

import com.guimarobo.Fintrack.model.Transaction;
import com.guimarobo.Fintrack.model.User;
import com.guimarobo.Fintrack.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody Transaction transaction,
                                                         @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.save(transaction, user));
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.findAll(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id,
                                                          @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.findById(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id,
                                                         @Valid @RequestBody Transaction updatedTransaction,
                                                         @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.update(id, updatedTransaction, user));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Transaction> patchTransaction(@PathVariable Long id,
                                                        @RequestBody Map<String, String> fields,
                                                        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.patch(id, fields, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id,
                                                  @AuthenticationPrincipal User user) {
        transactionService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}