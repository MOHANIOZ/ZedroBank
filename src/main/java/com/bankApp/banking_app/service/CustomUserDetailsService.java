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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Database-la username irukkannu thedurom (Optional use panrom)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User kidaikala boss: " + username));

        // 2. Database-la irukira User-ai Spring Security-ku puriyura "UserDetails" format-ku mathurom
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>() // Ippo kaaliya Roles (Authorities) anuprom
        );
    }
}