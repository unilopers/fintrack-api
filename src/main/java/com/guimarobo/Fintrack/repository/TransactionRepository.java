package com.guimarobo.Fintrack.repository;

import com.guimarobo.Fintrack.model.Transaction;
import com.guimarobo.Fintrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountUser(User user);
    Optional<Transaction> findByIdAndAccountUser(Long id, User user);
}