package com.guimarobo.Fintrack.controller;

import com.guimarobo.Fintrack.model.User;
import com.guimarobo.Fintrack.service.UserService;
import com.guimarobo.Fintrack.worker.AuditWorker;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AuditWorker auditWorker;

    public UserController(UserService userService, AuditWorker auditWorker) {
        this.userService = userService;
        this.auditWorker = auditWorker;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getAuthenticatedUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.findById(user.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateAuthenticatedUser(@AuthenticationPrincipal User user,
                                                        @RequestBody User updatedUser) {
        User savedUser = userService.update(user.getId(), updatedUser);

        // dispara auditoria em background — response já foi pro cliente
        auditWorker.registrarAuditoria(
                user.getId(),
                "PUT /users/me",
                "nome atualizado para: " + savedUser.getName()
        );

        return ResponseEntity.ok(savedUser);
    }

    @PatchMapping("/me")
    public ResponseEntity<User> patchAuthenticatedUser(@AuthenticationPrincipal User user,
                                                       @RequestBody Map<String, String> fields) {
        User updatedUser = userService.patch(user.getId(), fields);

        auditWorker.registrarAuditoria(
                user.getId(),
                "PATCH /users/me",
                "campos alterados: " + fields.keySet()
        );

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAuthenticatedUser(@AuthenticationPrincipal User user) {
        Long userId = user.getId();
        userService.delete(userId);

        // captura o ID antes de deletar — user não existe mais depois
        auditWorker.registrarAuditoria(
                userId,
                "DELETE /users/me",
                "conta removida permanentemente"
        );

        return ResponseEntity.noContent().build();
    }
}
