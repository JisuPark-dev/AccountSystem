package com.zerobase.Account.service;

import com.zerobase.Account.domain.Account;
import com.zerobase.Account.domain.AccountStatus;
import com.zerobase.Account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    @Transactional
    public void createAccount(Long userId, Long initialBalance) {

    }

    @Transactional
    public Account findAccount(Long id) {
        return accountRepository.findById(id).get();
    }
}
