package com.guimarobo.Fintrack.service;

import com.guimarobo.Fintrack.exception.NotFoundException;
import com.guimarobo.Fintrack.model.Account;
import com.guimarobo.Fintrack.model.Transaction;
import com.guimarobo.Fintrack.model.User;
import com.guimarobo.Fintrack.repository.AccountRepository;
import com.guimarobo.Fintrack.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<Transaction> findAll(User user) {
        return transactionRepository.findByAccountUser(user);
    }

    @Override
    public Transaction findById(Long id, User user) {
        return transactionRepository.findByIdAndAccountUser(id, user)
                .orElseThrow(() -> new NotFoundException("Transação não encontrada."));
    }

    @Override
    @Transactional
    public Transaction save(Transaction transaction, User user) {
        if (transaction.getAccount() == null || transaction.getAccount().getId() == null) {
            throw new IllegalArgumentException("A conta relacionada é obrigatória.");
        }

        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
        }

        Account account = accountRepository.findByIdAndUser(transaction.getAccount().getId(), user)
                .orElseThrow(() -> new NotFoundException("Conta não encontrada."));

        applyBalanceChange(account, transaction.getType(), transaction.getAmount());
        accountRepository.save(account);

        transaction.setAccount(account);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction update(Long id, Transaction updatedTransaction, User user) {
        if (updatedTransaction.getAccount() == null || updatedTransaction.getAccount().getId() == null) {
            throw new IllegalArgumentException("A conta relacionada é obrigatória.");
        }

        if (updatedTransaction.getAmount() == null || updatedTransaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
        }

        Transaction existingTransaction = findById(id, user);

        Account oldAccount = existingTransaction.getAccount();
        Account newAccount = accountRepository.findByIdAndUser(updatedTransaction.getAccount().getId(), user)
                .orElseThrow(() -> new NotFoundException("Conta não encontrada."));

        revertBalanceChange(oldAccount, existingTransaction.getType(), existingTransaction.getAmount());

        existingTransaction.setDescription(updatedTransaction.getDescription());
        existingTransaction.setAmount(updatedTransaction.getAmount());
        existingTransaction.setType(updatedTransaction.getType());
        existingTransaction.setDate(updatedTransaction.getDate());
        existingTransaction.setAccount(newAccount);

        applyBalanceChange(newAccount, updatedTransaction.getType(), updatedTransaction.getAmount());

        accountRepository.save(oldAccount);
        if (!oldAccount.getId().equals(newAccount.getId())) {
            accountRepository.save(newAccount);
        }

        return transactionRepository.save(existingTransaction);
    }

    @Override
    @Transactional
    public Transaction patch(Long id, Map<String, String> fields, User user) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Nenhum campo informado para atualização.");
        }

        String newDescription = null;
        BigDecimal newAmount = null;
        String newType = null;
        LocalDate newDate = null;
        Account newAccount = null;
        boolean accountChanged = false;

        if (fields.containsKey("description")) {
            newDescription = fields.get("description");
            if (newDescription == null || newDescription.isBlank()) {
                throw new IllegalArgumentException("A descrição não pode ser vazia.");
            }
        }

        if (fields.containsKey("amount")) {
            String amountStr = fields.get("amount");
            if (amountStr == null || amountStr.isBlank()) {
                throw new IllegalArgumentException("O valor não pode ser vazio.");
            }
            try {
                newAmount = new BigDecimal(amountStr);
                if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Valor inválido. Informe um número válido.");
            }
        }

        if (fields.containsKey("type")) {
            newType = fields.get("type");
            if (newType == null || newType.isBlank()) {
                throw new IllegalArgumentException("O tipo da transação não pode ser vazio.");
            }
            if (!newType.equalsIgnoreCase("DESPESA") && !newType.equalsIgnoreCase("ENTRADA")) {
                throw new IllegalArgumentException("Tipo de transação inválido: '" + newType + "'. Use 'DESPESA' ou 'ENTRADA'.");
            }
            newType = newType.toUpperCase();
        }

        if (fields.containsKey("date")) {
            String dateStr = fields.get("date");
            if (dateStr == null || dateStr.isBlank()) {
                throw new IllegalArgumentException("A data não pode ser vazia.");
            }
            try {
                newDate = LocalDate.parse(dateStr);
            } catch (java.time.format.DateTimeParseException e) {
                throw new IllegalArgumentException("Data inválida. Use o formato YYYY-MM-DD.");
            }
        }

        if (fields.containsKey("accountId")) {
            String accountIdStr = fields.get("accountId");
            if (accountIdStr == null || accountIdStr.isBlank()) {
                throw new IllegalArgumentException("O ID da conta não pode ser vazio.");
            }
            try {
                Long accountId = Long.parseLong(accountIdStr);
                newAccount = accountRepository.findByIdAndUser(accountId, user)
                        .orElseThrow(() -> new NotFoundException("Conta não encontrada."));
                accountChanged = true;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID de conta inválido. Informe um número válido.");
            }
        }

        Transaction existingTransaction = findById(id, user);
        boolean balanceAffected = newAmount != null || newType != null || accountChanged;
        Account oldAccount = existingTransaction.getAccount();

        if (balanceAffected) {
            revertBalanceChange(oldAccount, existingTransaction.getType(), existingTransaction.getAmount());
        }

        if (newDescription != null) existingTransaction.setDescription(newDescription);
        if (newAmount != null) existingTransaction.setAmount(newAmount);
        if (newType != null) existingTransaction.setType(newType);
        if (newDate != null) existingTransaction.setDate(newDate);
        if (accountChanged) existingTransaction.setAccount(newAccount);

        if (balanceAffected) {
            Account currentAccount = existingTransaction.getAccount();
            applyBalanceChange(currentAccount, existingTransaction.getType(), existingTransaction.getAmount());
            accountRepository.save(oldAccount);
            if (!oldAccount.getId().equals(currentAccount.getId())) {
                accountRepository.save(currentAccount);
            }
        }

        return transactionRepository.save(existingTransaction);
    }

    @Override
    @Transactional
    public void delete(Long id, User user) {
        Transaction transaction = findById(id, user);
        Account account = transaction.getAccount();
        revertBalanceChange(account, transaction.getType(), transaction.getAmount());
        accountRepository.save(account);
        transactionRepository.delete(transaction);
    }

    private void applyBalanceChange(Account account, String type, BigDecimal amount) {
        if (type.equalsIgnoreCase("DESPESA")) {
            account.setBalance(account.getBalance().subtract(amount));
        } else if (type.equalsIgnoreCase("ENTRADA")) {
            account.setBalance(account.getBalance().add(amount));
        } else {
            throw new IllegalArgumentException("Tipo de transação inválido: '" + type + "'. Use 'DESPESA' ou 'ENTRADA'.");
        }
    }

    private void revertBalanceChange(Account account, String type, BigDecimal amount) {
        if (type.equalsIgnoreCase("DESPESA")) {
            account.setBalance(account.getBalance().add(amount));
        } else if (type.equalsIgnoreCase("ENTRADA")) {
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            throw new IllegalArgumentException("Tipo de transação inválido: '" + type + "'. Use 'DESPESA' ou 'ENTRADA'.");
        }
    }
}