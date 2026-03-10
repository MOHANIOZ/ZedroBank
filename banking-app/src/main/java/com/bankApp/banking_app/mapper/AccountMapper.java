package com.bankApp.banking_app.mapper;

import com.bankApp.banking_app.dto.AccountDto;
import com.bankApp.banking_app.entity.Account;

public class AccountMapper {

    // Entity-ai DTO-va mathura logic
    public static AccountDto mapToAccountDto(Account account) {
        return new AccountDto(
                account.getId(),
                account.getAccountHolderName(),
                account.getBalance()
        );
    }

    // DTO-vai Entity-ya mathura logic (Thevai pattaal)
    public static Account mapToAccount(AccountDto accountDto) {
        Account account = new Account();
        account.setId(accountDto.getId());
        account.setAccountHolderName(accountDto.getAccountHolderName());
        account.setBalance(accountDto.getBalance());
        return account;
    }
}