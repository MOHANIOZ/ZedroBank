package com.bankApp.banking_app.controller;

import com.bankApp.banking_app.dto.AccountDto;
import com.bankApp.banking_app.dto.TransactionDto;
import com.bankApp.banking_app.mapper.AccountMapper;
import com.bankApp.banking_app.entity.Account;
import com.bankApp.banking_app.repository.TransactionRepository;
import com.bankApp.banking_app.repository.UserRepository;
import com.bankApp.banking_app.service.AccountService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bankApp.banking_app.entity.User;
import com.bankApp.banking_app.entity.Transaction;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    // Constructor Injection for required dependencies
    public AccountController(AccountService accountService,
                             UserRepository userRepository,
                             TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    // Endpoint to create a new bank account
    @PostMapping
    public Account addAccount(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    // Fetch details of a specific account by its ID
    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    // Retrieve a list of all existing accounts
    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    // Deposit funds into a specific account
    @PutMapping("/{id}/deposit")
    public ResponseEntity<AccountDto> deposit(@PathVariable Long id, @RequestBody TransactionDto request) {
        Double amount = request.getAmount();
        if (amount <= 0) {
            throw new RuntimeException("Deposit Amount must be positive");
        }

        // Update account balance via service and return mapped DTO
        Account updatedAccount = accountService.deposit(id, amount);
        return ResponseEntity.ok(AccountMapper.mapToAccountDto(updatedAccount));
    }

    // Withdraw funds from a specific account
    @PutMapping("/{id}/withdraw")
    public ResponseEntity<AccountDto> withdraw(@PathVariable Long id, @RequestBody TransactionDto request) {
        Double amount = request.getAmount();
        Account updatedAccount = accountService.withdraw(id, amount);
        return ResponseEntity.ok(AccountMapper.mapToAccountDto(updatedAccount));
    }

    // Securely transfer funds between accounts using the authenticated user's context
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(Principal principal, @RequestBody TransactionDto request) {
        // Retrieve the username of the logged-in user
        String username = principal.getName();

        Long fromAccountId = request.getFromAccountId();
        Long toAccountId = request.getToAccountId();
        Double amount = request.getAmount();

        // Perform the transfer operation
        accountService.transferFromMyAccount(username, fromAccountId, toAccountId, amount);

        return ResponseEntity.ok("Transfer successful from Account: " + fromAccountId + " to Account: " + toAccountId);
    }

    // Retrieve transaction history for the authenticated user with pagination and sorting
    @GetMapping("/my-transactions")
    public ResponseEntity<List<Transaction>> getMyTransactions(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Long accountId = user.getAccount().getId();

        // Create a Pageable object to handle pagination and sort by most recent transactions
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        List<Transaction> history = transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId, pageable);

        return ResponseEntity.ok(history);
    }

    // Administrative endpoint to delete an account by ID
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        accountService.delete(id);
        return "Account deleted successfully with ID: " + id;
    }

    // Self-service endpoint to delete the authenticated user's profile and account
    @DeleteMapping("/delete-me")
    public String delete(Principal principal) {
        String username = principal.getName();
        accountService.deleteMyAccount(username);
        return "Your Account and User profile deleted successfully!";
    }

    // Fetch the account details of the currently logged-in user
    @GetMapping("/my-account")
    public ResponseEntity<AccountDto> getMyAccountDetails(Principal principal) {
        String username = principal.getName();
        Account account = accountService.getMyAccount(username);
        return ResponseEntity.ok(AccountMapper.mapToAccountDto(account));
    }

    // Root endpoint for API health check or landing message
    @GetMapping("/")
    public String home() {
        return "ZedroBank is Live! Access APIs at /api/v1/accounts";
    }
}