package com.guimarobo.Fintrack.controller;

import com.guimarobo.Fintrack.dto.AccountResponse;
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
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody Account account,
                                                         @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(accountService.save(account, user)));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts(@AuthenticationPrincipal User user) {
        List<AccountResponse> responses = accountService.findAll(user).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id,
                                                          @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(toResponse(accountService.findById(id, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable Long id,
                                                         @RequestBody Account updatedAccount,
                                                         @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(toResponse(accountService.update(id, updatedAccount, user)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AccountResponse> patchAccount(@PathVariable Long id,
                                                        @RequestBody Map<String, String> fields,
                                                        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(toResponse(accountService.patch(id, fields, user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id,
                                              @AuthenticationPrincipal User user) {
        accountService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getBankName(),
                account.getAccountType(), account.getBalance());
    }
}
