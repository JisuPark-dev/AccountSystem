package com.zerobase.Account.controller;

import com.zerobase.Account.domain.Account;
import com.zerobase.Account.dto.AccountInfo;
import com.zerobase.Account.dto.CreateAccount;
import com.zerobase.Account.dto.DeleteAccount;
import com.zerobase.Account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/account")
    public CreateAccount.Response createAccount(
            @RequestBody @Valid CreateAccount.Request request
            ) {
        return CreateAccount.Response.from(
                accountService.createAccount(
                        request.getUserId()
                        , request.getInitialBalance()
                ) // TODO: test 투두
                // FIXME: test fixme
        );
    }

    @DeleteMapping("/account")
    public DeleteAccount.Response deleteAccount(
            @RequestBody @Valid DeleteAccount.Request request
    ) {
        return DeleteAccount.Response.from(
                accountService.deleteAccount(
                        request.getUserId(),
                        request.getAccountNumber()
                )
        );
    }

    @GetMapping("/account")
    public List<AccountInfo> getAccountsByUserId(
            @RequestParam("user_id") Long userId
    ) {
        return accountService.getAccountByUserId(userId).stream()
                .map(accountDto -> AccountInfo.builder()
                        .accountNumber(accountDto.getAccountNumber())
                        .balance(accountDto.getBalance()).build())
                .collect(Collectors.toList());
    }





    @GetMapping("account/{id}")
    public Account findAccount(@PathVariable Long id) {
        return accountService.findAccount(id);
    }
}
