package com.guimarobo.Fintrack.service;

import com.guimarobo.Fintrack.exception.NotFoundException;
import com.guimarobo.Fintrack.model.Account;
import com.guimarobo.Fintrack.model.User;
import com.guimarobo.Fintrack.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public List<Account> findAll(User user) {
        return accountRepository.findByUser(user);
    }

    @Override
    public Account findById(Long id, User user) {
        return accountRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Conta não encontrada."));
    }

    @Override
    @Transactional
    public Account save(Account account, User user) {
        account.setUser(user);
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account update(Long id, Account updatedAccount, User user) {
        Account existingAccount = findById(id, user);
        existingAccount.setBankName(updatedAccount.getBankName());
        existingAccount.setAccountType(updatedAccount.getAccountType());
        existingAccount.setBalance(updatedAccount.getBalance());
        return accountRepository.save(existingAccount);
    }

    @Override
    @Transactional
    public Account patch(Long id, Map<String, String> fields, User user) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Nenhum campo informado para atualização.");
        }

        Account existingAccount = findById(id, user);

        if (fields.containsKey("bankName")) {
            String bankName = fields.get("bankName");
            if (bankName == null || bankName.isBlank()) {
                throw new IllegalArgumentException("O nome do banco não pode ser vazio.");
            }
            existingAccount.setBankName(bankName);
        }

        if (fields.containsKey("accountType")) {
            String accountType = fields.get("accountType");
            if (accountType == null || accountType.isBlank()) {
                throw new IllegalArgumentException("O tipo da conta não pode ser vazio.");
            }
            existingAccount.setAccountType(accountType);
        }

        if (fields.containsKey("balance")) {
            String balanceStr = fields.get("balance");
            if (balanceStr == null || balanceStr.isBlank()) {
                throw new IllegalArgumentException("O saldo não pode ser vazio.");
            }
            try {
                existingAccount.setBalance(new BigDecimal(balanceStr));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Saldo inválido. Informe um número válido.");
            }
        }

        return accountRepository.save(existingAccount);
    }

    @Override
    @Transactional
    public void delete(Long id, User user) {
        Account account = findById(id, user);
        accountRepository.delete(account);
    }
}