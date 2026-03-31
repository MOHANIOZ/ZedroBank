package com.bankApp.banking_app.service;

import com.bankApp.banking_app.entity.Account;
import com.bankApp.banking_app.entity.Transaction;
import com.bankApp.banking_app.entity.User;
import com.bankApp.banking_app.repository.TransactionRepository;
import com.bankApp.banking_app.repository.UserRepository;
import com.bankApp.banking_app.exception.AccountNotFoundException;
import com.bankApp.banking_app.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmailService emailService;

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    // Constructor Injection for core repositories
    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Processes a deposit, updates the balance, records the transaction,
     * and sends a credit alert email.
     */
    @Transactional
    public Account deposit(Long id, Double amount) {
        if (amount <= 0) throw new RuntimeException("Amount must be positive");

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        // Calculate and update the new balance
        Double updatedBalance = account.getBalance() + amount;
        account.setBalance(updatedBalance);

        // Record the transaction details
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(id);
        transaction.setAmount(amount);
        transaction.setTransactionType("DEPOSIT");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("SUCCESS");
        transactionRepository.save(transaction);

        // Send a transaction alert email
        String body = "Dear User, your account has been credited with " + amount +
                ". Your current balance is " + updatedBalance;
        emailService.sendEmail("kmohankmohan462@gmail.com", "Bank Credit Alert", body);

        return accountRepository.save(account);
    }

    /**
     * Processes a withdrawal after validating the balance and records the transaction.
     */
    @Transactional
    public Account withdraw(Long id, Double amount) {
        if (amount <= 0) throw new RuntimeException("Amount must be positive");

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (account.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance for withdrawal!");
        }

        account.setBalance(account.getBalance() - amount);

        // Create transaction history entry
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(id);
        transaction.setAmount(amount);
        transaction.setTransactionType("WITHDRAW");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("SUCCESS");
        transactionRepository.save(transaction);

        return accountRepository.save(account);
    }

    /**
     * Facilitates a fund transfer between accounts with ownership validation and security checks.
     */
    @Transactional
    public void transferFromMyAccount(String username, Long fromAccountId, Long toId, Double amount) {
        // Prevent self-transfer validation
        if (fromAccountId.equals(toId)) {
            throw new RuntimeException("Transfer to the same account is not permitted. Please provide a different destination account.");
        }

        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Source account not found"));

        // Security check: Ensure the logged-in user owns the source account
        if (!fromAccount.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Access Denied: You are not authorized to perform transfers from this account.");
        }

        Account toAccount = accountRepository.findById(toId)
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        if (fromAccount.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance for this transfer.");
        }

        // Perform the balance adjustments
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        // Log the transfer transaction
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(fromAccount.getId());
        transaction.setToAccountId(toAccount.getId());
        transaction.setAmount(amount);
        transaction.setTransactionType("TRANSFER");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("SUCCESS");

        transactionRepository.save(transaction);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Send alert to the sender
        String subject = "Bank Alert: Transfer Successful!";
        String body = "Dear User,\n\nAmount: " + amount + " has been transferred from your account.\n" +
                "To Account ID: " + toId + "\n" +
                "New Balance: " + fromAccount.getBalance();

        emailService.sendEmail("kmohankmohan462@gmail.com", subject, body);
    }

    /**
     * Administrative delete: Removes user and their linked account via cascading.
     */
    @Transactional
    public void delete(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        userRepository.delete(account.getUser());
    }

    /**
     * User self-service delete: Removes the authenticated user and their account.
     */
    @Transactional
    public void deleteMyAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    /**
     * Retrieves the account associated with the authenticated username.
     */
    public Account getMyAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getAccount();
    }
}