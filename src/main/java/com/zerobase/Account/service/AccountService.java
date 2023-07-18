package com.zerobase.Account.service;


import com.zerobase.Account.domain.Account;
import com.zerobase.Account.domain.AccountUser;
import com.zerobase.Account.dto.AccountDto;

import java.util.List;

public interface AccountService {
    AccountDto createAccount(Long userId, Long initialBalance);

    AccountDto deleteAccount(Long userId, String accountNumber);

    void validateDeleteAccount(Account account, AccountUser accountUser);

    void validateCreateAccount(AccountUser accountUser);

    Account findAccount(Long id);

    List<AccountDto> getAccountByUserId(Long userId);

    AccountUser getAccountUser(Long userId);
}
