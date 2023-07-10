package com.zerobase.Account.service;

import com.zerobase.Account.domain.Account;
import com.zerobase.Account.domain.AccountStatus;
import com.zerobase.Account.repository.AccountRepository;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;
    
    @Test
    @DisplayName("계좌 조회 성공")
    void testXXX(){
        //given
        given(accountRepository.findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("40000")
                        .build()));
        //when
        Account account = accountService.findAccount(1234L);

        //then
        assertEquals("40000", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
        
    }

    @Test
    @DisplayName("테스트 이름 변경 1")
    void testGetAccount(){
        //given

        //when
        Account account = accountService.findAccount(1L);
        //then
        assertEquals(account.getAccountNumber(), "4000");
        assertEquals(account.getAccountStatus(), AccountStatus.IN_USE);

    }

    @Test
    @DisplayName("테스트 이름 변경 2")
    void testGetAccount2(){
        //given

        //when
        Account account = accountService.findAccount(2L);
        //then
        assertEquals(account.getAccountNumber(), "4000");
        assertEquals(account.getAccountStatus(), AccountStatus.IN_USE);

    }

}