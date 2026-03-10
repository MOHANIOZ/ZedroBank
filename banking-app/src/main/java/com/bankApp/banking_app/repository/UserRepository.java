package com.bankApp.banking_app.repository;

import com.bankApp.banking_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByUsername(String username); // inga enn optional use pandrom na it because ippa user ulla object illa na null return avum apadi achina NullPointer exception throw pannum, but optional use pandrathala apadi agathu null or empty ya erunthalum
}
