package com.guimarobo.Fintrack.repository;

import com.guimarobo.Fintrack.model.Transaction;
import com.guimarobo.Fintrack.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountUser(User user);
    Page<Transaction> findByAccountUser(User user, Pageable pageable);
    Optional<Transaction> findByIdAndAccountUser(Long id, User user);

    List<Transaction> findByAccountUserAndDateBetween(User user, LocalDate inicio, LocalDate fim);

    @Modifying
    @Query("UPDATE Transaction t SET t.category = :category WHERE t.id = :id")
    void updateCategory(@Param("id") Long id, @Param("category") String category);
}