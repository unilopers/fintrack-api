package com.guimarobo.Fintrack.controller;

import com.guimarobo.Fintrack.dto.UserResponse;
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

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getAuthenticatedUser(@AuthenticationPrincipal User user) {
        User found = userService.findById(user.getId());
        return ResponseEntity.ok(toResponse(found));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateAuthenticatedUser(@AuthenticationPrincipal User user,
                                                                @RequestBody User updatedUser) {
        User savedUser = userService.update(user.getId(), updatedUser);
        return ResponseEntity.ok(toResponse(savedUser));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> patchAuthenticatedUser(@AuthenticationPrincipal User user,
                                                               @RequestBody Map<String, String> fields) {
        User updatedUser = userService.patch(user.getId(), fields);
        return ResponseEntity.ok(toResponse(updatedUser));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAuthenticatedUser(@AuthenticationPrincipal User user) {
        userService.delete(user.getId());
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
