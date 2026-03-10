package com.bankApp.banking_app.controller;

import com.bankApp.banking_app.dto.UserDto;
import com.bankApp.banking_app.entity.Account;
import com.bankApp.banking_app.entity.User;
import com.bankApp.banking_app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody UserDto userDto) {
        // 1. User-ai create panrom
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole("ROLE_USER");

        // 2. Avarukku oru pudhu Account assign panrom
        Account account = new Account();
        account.setAccountHolderName(userDto.getUsername());
        account.setBalance(0.0);
        account.setUser(user); // Account-ai User kooda link panrom

        user.setAccount(account); // User-ai Account kooda link panrom

        userRepository.save(user); // User-ai save panna automatic-ah Account-um save aagidum (Cascade vachathala)

        return "User and Account created successfully!";
    }
}