package com.bankApp.banking_app.controller;

import com.bankApp.banking_app.dto.UserDto;
import com.bankApp.banking_app.entity.Account;
import com.bankApp.banking_app.entity.User;
import com.bankApp.banking_app.repository.UserRepository;
import com.bankApp.banking_app.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates a user and returns a JWT token on success.
     * The frontend stores this token and sends it as "Authorization: Bearer <token>"
     * on every subsequent request.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        return userRepository.findByUsername(loginRequest.getUsername())
                .map(user -> {
                    if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                        // ✅ Generate JWT token for the authenticated user
                        String token = jwtUtil.generateToken(user.getUsername());

                        // Return token + basic user info the frontend needs
                        return ResponseEntity.ok(Map.of(
                            "token", token,
                            "username", user.getUsername(),
                            "id", user.getId()
                        ));
                    } else {
                        return ResponseEntity.status(401).body(Map.of("error", "Invalid password"));
                    }
                })
                .orElse(ResponseEntity.status(401).body(Map.of("error", "User not found")));
    }

    /**
     * Registers a new user and automatically creates a linked bank account.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {
        // Check if username already exists
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "Username already taken"));
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole("ROLE_USER");

        Account account = new Account();
        account.setAccountHolderName(userDto.getUsername());
        account.setBalance(0.0);
        account.setUser(user);
        user.setAccount(account);

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User and Account created successfully!"));
    }
}
