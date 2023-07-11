package com.zerobase.Account.controller;

import com.zerobase.Account.domain.Account;
import com.zerobase.Account.dto.CreateAccount;
import com.zerobase.Account.service.AccountService;
import com.zerobase.Account.service.RedisTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final RedisTestService redisTestService;

    @PostMapping("/account")
    public CreateAccount.Response createAccount(
            @RequestBody @Valid CreateAccount.Request request
            ) {
        accountService.createAccount();
        return new CreateAccount.Response();
    }

    @GetMapping("/get-lock")
    public String getLock() {
        return redisTestService.getLock();
    }



    @GetMapping("account/{id}")
    public Account findAccount(@PathVariable Long id) {
        return accountService.findAccount(id);
    }
}
