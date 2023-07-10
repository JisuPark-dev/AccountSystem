package com.zerobase.Account.service;

import com.zerobase.Account.domain.Account;
import com.zerobase.Account.domain.AccountStatus;
import com.zerobase.Account.repository.AccountRepository;
import net.bytebuddy.asm.Advice;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

        //when
        Account account = accountService.findAccount(1234L);

        //then
        verify(accountRepository, times(1)).findById(captor.capture());
        verify(accountRepository, times(0)).save(any());

        assertEquals(captor.getValue(),1234L);
        assertEquals("40000", account.getAccountNumber());
        assertNotEquals("40001", account.getAccountNumber());
        assertTrue(1234L== captor.getValue());
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