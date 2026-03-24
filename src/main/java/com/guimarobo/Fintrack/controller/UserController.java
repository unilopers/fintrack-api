package com.guimarobo.Fintrack.controller;

import com.guimarobo.Fintrack.model.User;
import com.guimarobo.Fintrack.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET - Buscar dados do próprio usuário autenticado
    @GetMapping("/me")
    public ResponseEntity<User> getAuthenticatedUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.findById(user.getId()));
    }

    // PUT - Atualizar dados do próprio usuário autenticado
    @PutMapping("/me")
    public ResponseEntity<User> updateAuthenticatedUser(@AuthenticationPrincipal User user,
                                                        @RequestBody User updatedUser) {
        User savedUser = userService.update(user.getId(), updatedUser);
        return ResponseEntity.ok(savedUser);
    }

    // PATCH - Atualizar parcialmente dados do próprio usuário autenticado
    @PatchMapping("/me")
    public ResponseEntity<User> patchAuthenticatedUser(@AuthenticationPrincipal User user,
                                                       @RequestBody Map<String, String> fields) {
        User updatedUser = userService.patch(user.getId(), fields);
        return ResponseEntity.ok(updatedUser);
    }

    // DELETE - Deletar a própria conta
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAuthenticatedUser(@AuthenticationPrincipal User user) {
        userService.delete(user.getId());
        return ResponseEntity.noContent().build();
    }
}
