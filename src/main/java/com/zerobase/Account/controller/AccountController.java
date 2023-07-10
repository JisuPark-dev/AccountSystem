package com.zerobase.Account.controller;

import com.zerobase.Account.domain.Account;
import com.zerobase.Account.service.AccountService;
import com.zerobase.Account.service.RedisTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final RedisTestService redisTestService;

    @GetMapping("/get-lock")
    public String getLock() {
        return redisTestService.getLock();
    }

    @GetMapping("/create-account")
    public String createAccount() {
        accountService.saveAccount();
        return "ok";
    }

    @GetMapping("account/{id}")
    public Account findAccount(@PathVariable Long id) {
        return accountService.findAccount(id);
    }
}
