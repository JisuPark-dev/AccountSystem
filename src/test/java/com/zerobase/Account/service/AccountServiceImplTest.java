package com.zerobase.Account.service;

import com.zerobase.Account.domain.Account;
import com.zerobase.Account.domain.AccountUser;
import com.zerobase.Account.dto.AccountDto;
import com.zerobase.Account.exception.AccountException;
import com.zerobase.Account.repository.AccountUserRepository;
import com.zerobase.Account.repository.AccountRepository;
import com.zerobase.Account.type.AccountStatus;
import com.zerobase.Account.type.ErrorCode;
import org.junit.jupiter.api.Assertions;
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

import static com.zerobase.Account.type.AccountStatus.IN_USE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @InjectMocks
    private AccountServiceImpl accountServiceImpl;



    @Test
    void creatFirstAccount(){
        //given
        AccountUser poby = AccountUser.builder()
                .name("poby").build();
        poby.setId(15L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.findFirstByAccountUserOrderByIdDesc(poby))
                .willReturn(Optional.empty());
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(poby)
                        .accountNumber("1000000013").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto account = accountServiceImpl.createAccount(1L, 1000L);
        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(account.getUserId(),15L);
        assertEquals(10,captor.getValue().getAccountNumber().length());
    }

    //이미 생성되어 있다면 1씩 더하면서 생성됨
    @Test
    void successCreatAccount(){
        //given
        AccountUser poby = AccountUser.builder()
                .name("poby").build();
        poby.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.findFirstByAccountUserOrderByIdDesc(poby))
                .willReturn(Optional.of(Account.builder()
                                .accountUser(poby)
                        .accountNumber("1000000012").build()));
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(poby)
                        .accountNumber("1000000013").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        
        //when
        AccountDto account = accountServiceImpl.createAccount(1L, 1000L);
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
        AccountDto account = accountServiceImpl.deleteAccount(1L, "1234567880");
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
                () -> accountServiceImpl.deleteAccount(1L, "1234567890"));

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
                () -> accountServiceImpl.deleteAccount(1L, "1234567890"));

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
                () -> accountServiceImpl.deleteAccount(1L, "1234567890"));

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
                () -> accountServiceImpl.deleteAccount(1L, "1234567890"));

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
                () -> accountServiceImpl.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());
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
                () -> accountServiceImpl.createAccount(1L, 1000L));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("이미 사용중인 계좌번호임 - 계좌 생성 실패")
    void createAccount_AccountNumber_Already_Exist() {
        //given
        AccountUser poby = AccountUser.builder()
                .name("poby").build();
        poby.setId(1L);

        Account existingAccount = Account.builder()
                .accountUser(poby)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000013").build();
        existingAccount.setId(1L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));

        given(accountRepository.findFirstByAccountUserOrderByIdDesc(poby))
                .willReturn(Optional.of(existingAccount));

        // 계좌 번호로 계좌를 조회했을 때 이미 해당 계좌번호로 생성된 계좌가 있음
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(existingAccount));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountServiceImpl.createAccount(1L, 1000L));

        //then
        assertEquals(ErrorCode.ACCOUNTNUMBER_ALREADY_USED, accountException.getErrorCode());
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
                () -> accountServiceImpl.createAccount(1L, 1000L));

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
        List<AccountDto> accountDtos = accountServiceImpl.getAccountByUserId(15L);
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
                () -> accountServiceImpl.getAccountByUserId(1L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
        
    }
}