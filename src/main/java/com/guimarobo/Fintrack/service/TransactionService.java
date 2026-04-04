package com.guimarobo.Fintrack.service;

import com.guimarobo.Fintrack.dto.TransactionReportResponse;
import com.guimarobo.Fintrack.model.Transaction;
import com.guimarobo.Fintrack.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface TransactionService {
    List<Transaction> findAll(User user);
    Page<Transaction> findAll(User user, Pageable pageable);
    Transaction findById(Long id, User user);
    Transaction save(Transaction transaction, User user);
    Transaction update(Long id, Transaction updatedTransaction, User user);
    Transaction patch(Long id, Map<String, String> fields, User user);
    void delete(Long id, User user);
    TransactionReportResponse generateReport(User user, int mes, int ano);
}