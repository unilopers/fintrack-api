package com.guimarobo.Fintrack.service;

import com.guimarobo.Fintrack.model.User;
import com.guimarobo.Fintrack.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> loadUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}