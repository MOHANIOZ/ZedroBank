package com.bankApp.banking_app.mapper;

import com.bankApp.banking_app.dto.AccountDto;
import com.bankApp.banking_app.entity.Account;

public class AccountMapper {

    /**
     * Maps an Account entity to an AccountDto.
     * Used to send data to the client while hiding internal entity details.
     */
    public static AccountDto mapToAccountDto(Account account) {
        return new AccountDto(
                account.getId(),
                account.getAccountHolderName(),
                account.getBalance()
        );
    }

    /**
     * Maps an AccountDto back to an Account entity.
     * Used when converting incoming request data into a persistence-ready object.
     */
    public static Account mapToAccount(AccountDto accountDto) {
        Account account = new Account();
        account.setId(accountDto.getId());
        account.setAccountHolderName(accountDto.getAccountHolderName());
        account.setBalance(accountDto.getBalance());
        return account;
    }
}