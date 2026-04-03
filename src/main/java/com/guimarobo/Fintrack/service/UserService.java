package com.guimarobo.Fintrack.service;

import com.guimarobo.Fintrack.model.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    List<User> findAll();
    User findById(Long id);
    User register(String name, String email, String password);
    User save(User user);
    User update(Long id, User updatedUser);
    User patch(Long id, Map<String, String> fields);
    void delete(Long id);
}
