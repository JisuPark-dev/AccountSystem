package com.zerobase.Account.service;

import com.zerobase.Account.domain.Account;
import com.zerobase.Account.domain.AccountUser;
import com.zerobase.Account.dto.AccountDto;
import com.zerobase.Account.exception.AccountException;
import com.zerobase.Account.repository.AccountUserRepository;
import com.zerobase.Account.repository.AccountRepository;
import com.zerobase.Account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @Mock
    private AccountUserRepository accountUserRepository;
    @InjectMocks
    private AccountService accountService;

    @Test
    void creatAccountSuccess(){
        //given
        AccountUser poby = AccountUser.builder()
                .id(12L)
                .name("poby").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                                .accountUser(poby)
                        .accountNumber("1000000012").build()));
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(poby)
                        .accountNumber("1000000013").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        
        //when
        AccountDto account = accountService.createAccount(1L, 1000L);
        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(account.getUserId(),12L);
        assertEquals("1000000013",captor.getValue().getAccountNumber());
    }

    @Test
    void creatFirstAccount(){
        //given
        AccountUser poby = AccountUser.builder()
                .id(15L)
                .name("poby").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(poby)
                        .accountNumber("1000000013").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto account = accountService.createAccount(1L, 1000L);
        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(account.getUserId(),15L);
        assertEquals("1000000000",captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 생성 실")
    void creatAccount_UserNotFound(){
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }
    @Test
    void createAccount_maxAccountIs10(){
        //given
        AccountUser poby = AccountUser.builder()
                .id(15L)
                .name("poby").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        //then
        assertEquals(ErrorCode.MAX_COUNT_FOR_USER_10, accountException.getErrorCode());
        
    }

}