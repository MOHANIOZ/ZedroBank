package com.bankApp.banking_app.service;

import com.bankApp.banking_app.entity.User;
import com.bankApp.banking_app.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Locates the user based on the username.
     * This method is used by Spring Security to perform authentication.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Search for the user in the database; throw an exception if not found
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Convert our custom User entity into Spring Security's UserDetails object
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>() // Currently passing an empty list of authorities/roles
        );
    }
}