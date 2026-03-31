package com.bankApp.banking_app.dto;

import lombok.Data;

@Data
public class TransactionDto {
    private Long fromAccountId;
    private Long toAccountId;
    private Double amount;
}