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
//import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    private EmailService emailService;

    private final UserRepository userRepository;

    private final TransactionRepository transactionRepository; // Field-ah add panni constructor-la inject pannunga

    // 2. Constructor-la idhaiyum sethu inject pannanum
    public AccountService(AccountRepository accountRepository, UserRepository userRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException("Account is not Found"));
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account deposit(Long id, Double amount) {
        if (amount <= 0) throw new RuntimeException("Amount must be positive");

        Account account = accountRepository.findById(id).orElseThrow();

        // 1. Inga dhaan variable-ai declare pannanum (Red color ippo poyidum)
        Double updatedBalance = account.getBalance() + amount;
        account.setBalance(updatedBalance);

        // Transaction Record
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(id);
        transaction.setAmount(amount);
        transaction.setTransactionType("DEPOSIT");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("SUCCESS");
        transactionRepository.save(transaction);

        // 2. Email Logic - Inga "debited" badhula "credited" nu maathunga (Deposit na credit dhaan)
        String body = "Hi User, your account has been credited with " + amount + ". Current balance is " + updatedBalance;

        // 3. User-oda real email-ai inga "account.getUser().getUsername()" nu poodalaam
        emailService.sendEmail("kmohankmohan462@gmail.com", "Bank Transaction Alert", body);

        return accountRepository.save(account);
    }

    public Account withdraw(Long id, Double amount) {
        if (amount <= 0) throw new RuntimeException("Amount must be positive");

        Account account = accountRepository.findById(id).orElseThrow();

        if (account.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance for withdrawal!");
        }

        account.setBalance(account.getBalance() - amount);

        Transaction transaction = new Transaction();
        transaction.setFromAccountId(id);
        transaction.setAmount(amount);
        transaction.setTransactionType("WITHDRAW"); // Type: WITHDRAW
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("SUCCESS");
        transactionRepository.save(transaction);

        return accountRepository.save(account);
    }

    @Transactional
    public void transferFromMyAccount(String username, Long fromAccountId, Long toId, Double amount) {
        // Logic-kulla modhalla idhai poodunga
        if (fromAccountId.equals(toId)) {
            throw new RuntimeException("Sondha account-ke transfer panna mudiyaadhu boss! Vera Account ID kudunga.");
        }
        // 1. From Account-ai find panni, adhu login panna user-odadhu thaana-nu check panrom
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // SECURITY CHECK:
        // User login pannavum, fromAccount-oda owner-um match aaganum!
        if (!fromAccount.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Access Denied: Neenga indha account-oda owner illa!");
        }

        // 2. To Account-ai fetch panrom
        Account toAccount = accountRepository.findById(toId)
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        // 3. Balance Check
        if (fromAccount.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance boss!");
        }

        // 4. Transfer Logic
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        // 5. Transaction Record (Type: TRANSFER)
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(fromAccount.getId());
        transaction.setToAccountId(toAccount.getId());
        transaction.setAmount(amount);
        transaction.setTransactionType("TRANSFER");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("SUCCESS");

        // Save all
        transactionRepository.save(transaction);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // EMAIL ALERT LOGIC:
        String subject = "Bank Alert: Transaction Successful!";
        String body = "Dear User, \n\n" +
                "Amount: " + amount + " has been transferred from your account.\n" +
                "To Account ID: " + toId + "\n" +
                "Current Balance: " + fromAccount.getBalance();

         // Receiver-kum email anuppalam (Optionally)
        emailService.sendEmail("kmohankmohan462@gmail.com", subject, body);
    }

    @Transactional
    public void delete(Long id) {
        // 1. Account irukka-nu check panrom
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // 2. Account-oda user-ai kandupudichu, user-ai delete panrom
        // User-ai delete panna cascade vachirundha account-um automatic-ah delete aagidum
        userRepository.delete(account.getUser());
    }
    @Transactional
    public void deleteMyAccount(String username) {
        // 1. User-ai find pannalaam
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. User-ai delete panna, avar account-um delete aagidum
        userRepository.delete(user);
    }
    public Account getMyAccount(String username) {
        // 1. First user-ai kandupudikirom
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Andha user-kku link aagi irukira account-ai thiruppi anuprom
        return user.getAccount();
    }
}
