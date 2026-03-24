package com.guimarobo.Fintrack.service;

import com.guimarobo.Fintrack.model.Transaction;
import com.guimarobo.Fintrack.model.User;

import java.util.List;
import java.util.Map;

public interface TransactionService {
    List<Transaction> findAll(User user);
    Transaction findById(Long id, User user);
    Transaction save(Transaction transaction, User user);
    Transaction update(Long id, Transaction updatedTransaction, User user);
    Transaction patch(Long id, Map<String, String> fields, User user);
    void delete(Long id, User user);
}