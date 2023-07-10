package com.zerobase.Account.controller;

import com.zerobase.Account.domain.Account;
import com.zerobase.Account.domain.AccountStatus;
import com.zerobase.Account.service.AccountService;
import com.zerobase.Account.service.RedisTestService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
    @MockBean
    private AccountService accountService;

    @MockBean
    private RedisTestService redisTestService;

    @Autowired
    private MockMvc mocMvc;

    @Test
    void successGetAccount() throws Exception {
        //given
        given(accountService.findAccount(anyLong()))
                .willReturn(Account.builder()
                        .accountNumber("1234")
                        .accountStatus(AccountStatus.IN_USE)
                        .build());
        //when
        //then
        mocMvc.perform(get("/account/876"))
                .andDo(print())
                .andExpect(jsonPath("$.accountNumber").value("1234"))
                .andExpect(jsonPath("$.accountStatus").value("IN_USE"))
                .andExpect(status().isOk());

    }


}