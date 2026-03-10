package com.bankApp.banking_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
//@NoArgsConstructor
public class AccountDto {
    private Long id;
    private String accountHolderName;
    private Double balance;

    public AccountDto() {
    }
}

