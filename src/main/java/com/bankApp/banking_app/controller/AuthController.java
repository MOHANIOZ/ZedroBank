package com.bankApp.banking_app.controller;

import com.bankApp.banking_app.dto.UserDto;
import com.bankApp.banking_app.entity.Account;
import com.bankApp.banking_app.entity.User;
import com.bankApp.banking_app.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection for repository and password encoder
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticates a user based on username and password.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        // 1. Find the user in the database by their username
        // Using Optional to handle 'User Not Found' safely
        return userRepository.findByUsername(loginRequest.getUsername())
                .map(user -> {
                    // 2. Check if the raw password from request matches the hashed password in DB
                    if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {

                        // 3. SUCCESS: Return 200 OK with the User object
                        // This allows the frontend to see user details (like ID or Role)
                        return ResponseEntity.ok(user);

                    } else {
                        // 4. FAILURE: Password does not match
                        return ResponseEntity.status(401).body("Invalid Password");
                    }
                })
                // 5. FAILURE: Username does not exist in the database
                .orElse(ResponseEntity.status(401).body("User not found"));
    }

    /**
     * Registers a new user and automatically creates a linked bank account.
     */
    @PostMapping("/register")
    public String registerUser(@RequestBody UserDto userDto) {
        // Initialize a new User entity and encode the password for security
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole("ROLE_USER");

        // Create a default Account for the new user
        Account account = new Account();
        account.setAccountHolderName(userDto.getUsername());
        account.setBalance(0.0);

        // Establish the bidirectional relationship between User and Account
        account.setUser(user);
        user.setAccount(account);

        // Save the user; the account is persisted automatically via CascadeType.ALL
        userRepository.save(user);

        return "User and Account created successfully!";
    }
}