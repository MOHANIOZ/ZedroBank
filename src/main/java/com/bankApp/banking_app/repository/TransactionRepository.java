package com.bankApp.banking_app.repository;

import com.bankApp.banking_app.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable; // Indha import mukkiyam


import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // fetch the specific account transaction
    List<Transaction> findByFromAccountIdOrToAccountId(Long fromId, Long toId, Pageable pageable);
}