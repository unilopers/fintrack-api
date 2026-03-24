package com.guimarobo.Fintrack.controller;

import com.guimarobo.Fintrack.model.Account;
import com.guimarobo.Fintrack.model.User;
import com.guimarobo.Fintrack.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account,
                                                 @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.save(account, user));
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.findAll(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id,
                                                  @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.findById(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id,
                                                 @Valid @RequestBody Account updatedAccount,
                                                 @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.update(id, updatedAccount, user));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Account> patchAccount(@PathVariable Long id,
                                                @RequestBody Map<String, String> fields,
                                                @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.patch(id, fields, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id,
                                              @AuthenticationPrincipal User user) {
        accountService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}