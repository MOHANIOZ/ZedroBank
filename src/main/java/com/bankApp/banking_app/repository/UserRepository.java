package com.bankApp.banking_app.repository;

import com.bankApp.banking_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{
    // Using Optional if we provide a null object i will not throw a nullPointer exception
    Optional<User> findByUsername(String username);
}
