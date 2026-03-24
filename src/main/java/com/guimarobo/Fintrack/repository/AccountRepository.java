package com.guimarobo.Fintrack.repository;

import com.guimarobo.Fintrack.model.Account;
import com.guimarobo.Fintrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    Optional<Account> findByIdAndUser(Long id, User user);
}