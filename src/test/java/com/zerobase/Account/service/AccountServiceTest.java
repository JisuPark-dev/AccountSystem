package com.zerobase.Account.service;

import com.zerobase.Account.domain.Account;
import com.zerobase.Account.domain.AccountUser;
import com.zerobase.Account.dto.AccountDto;
import com.zerobase.Account.exception.AccountException;
import com.zerobase.Account.repository.AccountUserRepository;
import com.zerobase.Account.repository.AccountRepository;
import com.zerobase.Account.type.AccountStatus;
import com.zerobase.Account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    void successCreatAccount(){
        //given
        AccountUser poby = AccountUser.builder()
                .name("poby").build();
        poby.setId(12L);
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
    void successDeleteAccount(){
        //given
        AccountUser poby = AccountUser.builder()
                .name("poby").build();
        poby.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(poby)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto account = accountService.deleteAccount(1L, "1234567880");
        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(account.getUserId(),12L);
        assertEquals("1000000012",captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED,captor.getValue().getAccountStatus());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void deleteAccountFailed_UserNotFound(){
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    void deleteAccountFailed_AccountNotFound(){
        //given
        AccountUser poby = AccountUser.builder()
                .name("poby").build();
        poby.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주가 다름 - 계좌 해지 실패")
    void deleteAccountFailed_UserUnMatched(){
        //given
        AccountUser poby = AccountUser.builder()
                .name("poby").build();
        poby.setId(12L);
        AccountUser anotherUser = AccountUser.builder()
                .name("pofy").build();
        anotherUser.setId(13L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(anotherUser)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.USER_ACCOUNT_UNMATCHED, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 잔액 남아있음 - 계좌 해지 실패")
    void deleteAccountFailed_Balance_Not_Empty(){
        //given
        AccountUser poby = AccountUser.builder()
                .name("poby").build();
        poby.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(poby)
                        .balance(1L)
                        .accountNumber("1000000012").build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, accountException.getErrorCode());
    }

    @Test
    @DisplayName("해지계좌는 해지할 수 없다  - 계좌 해지 실패")
    void deleteAccountFailed_AlreadyUnregistered(){
        //given
        AccountUser poby = AccountUser.builder()
                .name("poby").build();
        poby.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(poby)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());
    }

    @Test
    void creatFirstAccount(){
        //given
        AccountUser poby = AccountUser.builder()
                .name("poby").build();
        poby.setId(15L);
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
    @DisplayName("해당 유저 없음 - 계좌 생성 실패")
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
    @DisplayName("유저 당 최대 계좌 수는 10개")
    void createAccount_maxAccountIs10(){
        //given
        AccountUser poby = AccountUser.builder()
                .name("poby").build();
        poby.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        //then
        assertEquals(ErrorCode.MAX_COUNT_FOR_USER_10, accountException.getErrorCode());
        
    }
    
    @Test
    void successGetAccountByUserId(){
        //given
        AccountUser poby = AccountUser.builder()
                .name("poby").build();
        poby.setId(12L);
        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(poby)
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .build(),
                Account.builder()
                        .accountUser(poby)
                        .accountNumber("2222222222")
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountUser(poby)
                        .accountNumber("3333333333")
                        .balance(3000L)
                        .build()
        );
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);
        //when
        List<AccountDto> accountDtos = accountService.getAccountByUserId(15L);
        //then
        assertEquals(3, accountDtos.size());
        assertEquals("1111111111", accountDtos.get(0).getAccountNumber());
        assertEquals(1000,accountDtos.get(0).getBalance());
        assertEquals("2222222222", accountDtos.get(1).getAccountNumber());
        assertEquals(2000,accountDtos.get(1).getBalance());
        assertEquals("3333333333", accountDtos.get(2).getAccountNumber());
        assertEquals(3000,accountDtos.get(2).getBalance());
    }

    @Test
    void failedToGetAccounts(){
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.getAccountByUserId(1L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
        
    }
}