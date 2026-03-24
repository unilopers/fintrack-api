package com.guimarobo.Fintrack.service;

import com.guimarobo.Fintrack.exception.NotFoundException;
import com.guimarobo.Fintrack.model.User;
import com.guimarobo.Fintrack.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    @Override
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(Long id, User updatedUser) {
        User existingUser = findById(id);
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public User patch(Long id, Map<String, String> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Nenhum campo informado para atualização.");
        }

        User existingUser = findById(id);

        if (fields.containsKey("name")) {
            String name = fields.get("name");
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Nome não pode ser vazio.");
            }
            existingUser.setName(name);
        }

        if (fields.containsKey("email")) {
            String email = fields.get("email");
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email não pode ser vazio.");
            }
            existingUser.setEmail(email);
        }

        if (fields.containsKey("password")) {
            String password = fields.get("password");
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("A senha não pode ser vazia.");
            }
            existingUser.setPassword(passwordEncoder.encode(password));
        }

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.delete(findById(id));
    }
}
