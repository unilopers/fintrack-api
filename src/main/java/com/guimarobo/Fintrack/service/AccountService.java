package com.guimarobo.Fintrack.service;

import com.guimarobo.Fintrack.model.Account;
import com.guimarobo.Fintrack.model.User;

import java.util.List;
import java.util.Map;

public interface AccountService {
    List<Account> findAll(User user);
    Account findById(Long id, User user);
    Account save(Account account, User user);
    Account update(Long id, Account updatedAccount, User user);
    Account patch(Long id, Map<String, String> fields, User user);
    void delete(Long id, User user);
}