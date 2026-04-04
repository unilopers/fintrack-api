package com.guimarobo.Fintrack.service;

import com.guimarobo.Fintrack.model.Account;
import com.guimarobo.Fintrack.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface AccountService {
    List<Account> findAll(User user);
    Page<Account> findAll(User user, Pageable pageable);
    Account findById(Long id, User user);
    Account save(Account account, User user);
    Account update(Long id, Account updatedAccount, User user);
    Account patch(Long id, Map<String, String> fields, User user);
    void delete(Long id, User user);
}