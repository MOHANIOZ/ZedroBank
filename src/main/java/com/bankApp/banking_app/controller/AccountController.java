package com.bankApp.banking_app.controller;

import com.bankApp.banking_app.dto.AccountDto;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    // Constructor Injection (Indha block thaan red line-ai remove pannum)
    public AccountController(AccountService accountService,
                             UserRepository userRepository,
                             TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    // Puthiya account create panna
    @PostMapping
    public Account addAccount(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    // Oru account-ai pakka
    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    // Ella accounts-aiyum list panna
    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PutMapping("/{id}/deposit")
    public ResponseEntity<AccountDto> deposit(@PathVariable Long id, @RequestBody Map<String, Double> request) {

        Double amount = request.get("amount");
        if (amount <= 0) {
            throw new RuntimeException("Deposit Amount must be Positive");
        }
        // Service ippo Account-ah return pannum, so error varaadhu
        Account updatedAccount = accountService.deposit(id, amount);

        // Mapper vachu clean-ah anupuvom
        return ResponseEntity.ok(AccountMapper.mapToAccountDto(updatedAccount));
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<AccountDto> withdraw(@PathVariable Long id, @RequestBody Map<String, Double> request) {
        Double amount = request.get("amount");
        Account updatedAccount = accountService.withdraw(id, amount);
        return ResponseEntity.ok(AccountMapper.mapToAccountDto(updatedAccount));
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(Principal principal, @RequestBody Map<String, Object> request) {
        // 1. Yaaru login panni irukkanu check pannuvom (Security-kaga)
        String username = principal.getName();

        // 2. Body-la irundhu yendha account-la irundhu poganumnu vanguvom
        Long fromAccountId = Long.valueOf(request.get("from_account_id").toString());
        Long toAccountId = Long.valueOf(request.get("to_account_id").toString());
        Double amount = Double.valueOf(request.get("amount").toString());

        // 3. Oru extra safety check: Andha 'fromAccountId' namma 'Vijay' oda account thaana-nu
        // Service-la oru check pottuta innum nalladhu.
        accountService.transferFromMyAccount(username, fromAccountId, toAccountId, amount);
        return ResponseEntity.ok("Transfer successful from Account: " + fromAccountId);
    }
    @GetMapping("/my-transactions")
    public ResponseEntity<List<Transaction>> getMyTransactions(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Long accountId = user.getAccount().getId();

        // Pageable object create panrom (Sorting sethu)
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        List<Transaction> history = transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId, pageable);

        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        accountService.delete(id); // Ippo method name match aagum, red line poyidum
        return "Account deleted successfully with ID: " + id;
    }
    @DeleteMapping("/delete-me")
    public String delete(Principal principal) {
        String username = principal.getName();
        accountService.deleteMyAccount(username);
        return "Your Account and User profile deleted successfully!";
    }

    @GetMapping("/my-account")
    public ResponseEntity<AccountDto> getMyAccountDetails(Principal principal) {
        // principal.getName() kudutha, ippo login panni irukira username varum
        String username = principal.getName();

        Account account = accountService.getMyAccount(username);
        return ResponseEntity.ok(AccountMapper.mapToAccountDto(account));
    }
    @GetMapping("/")
    public String home() {
        return "ZedroBank is Live, Mohan! Access APIs at /api/v1/accounts";
    }
}