package com.zerobase.Account.service;

import com.zerobase.Account.domain.Account;
import com.zerobase.Account.domain.AccountUser;
import com.zerobase.Account.domain.Transaction;
import com.zerobase.Account.dto.TransactionDto;
import com.zerobase.Account.exception.AccountException;
import com.zerobase.Account.repository.AccountRepository;
import com.zerobase.Account.repository.AccountUserRepository;
import com.zerobase.Account.repository.TransactionRepository;
import com.zerobase.Account.type.AccountStatus;
import com.zerobase.Account.type.ErrorCode;
import com.zerobase.Account.type.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.zerobase.Account.type.AccountStatus.IN_USE;
import static com.zerobase.Account.type.TransactionResultType.F;
import static com.zerobase.Account.type.TransactionResultType.S;
import static com.zerobase.Account.type.TransactionType.CANCEL;
import static com.zerobase.Account.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    public static final Long CANCEL_AMOUNT = 200L;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void successUseBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("poby").build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        //when
        TransactionDto transactionDto = transactionService.useBalance(1L, "1000000000", 2000L);

        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(2000L, captor.getValue().getAmount());
        assertEquals(8000L, captor.getValue().getBalanceSnapshot());
        assertEquals(9000L, transactionDto.getBalanceSnapshot());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(USE, transactionDto.getTransactionType());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void UseBalance_UserNotFound(){
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890", 1000L));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    void UseBalance_AccountNotFound(){
        //given
        AccountUser poby = AccountUser.builder()
                .id(12L)
                .name("poby").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890",1000L));

        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주가 다름 - 계좌 해지 실패")
    void deleteAccountFailed_UserUnMatched(){
        //given
        AccountUser poby = AccountUser.builder()
                .id(12L)
                .name("poby").build();
        AccountUser anotherUser = AccountUser.builder()
                .id(13L)
                .name("pofy").build();
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
                () -> transactionService.useBalance(1L, "1234567890",1000L));

        //then
        assertEquals(ErrorCode.USER_ACCOUNT_UNMATCHED, accountException.getErrorCode());
    }
    @Test
    @DisplayName("해지계좌는 해지할 수 없다  - 계좌 해지 실패")
    void deleteAccountFailed_AlreadyUnregistered(){
        //given
        AccountUser poby = AccountUser.builder()
                .id(12L)
                .name("poby").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(poby));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(poby)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890",1000L));

        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());
    }

    @Test
    @DisplayName("거래 금액이 잔액보다 큰 경우")
    void exceedAmount_UseBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("poby").build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(100L)
                .accountNumber("1000000012").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        //then
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890",1000L));

        assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, accountException.getErrorCode());
        verify(transactionRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("실패 트랜젝션 저장 성")
    void saveFailedUseTransaction() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("poby").build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012").build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        //when
        transactionService.saveFailedUseTransaction("1000000000", 2000L);

        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(10000L, captor.getValue().getBalanceSnapshot());
        assertEquals(F, captor.getValue().getTransactionResultType());
        assertEquals(2000L, captor.getValue().getAmount());
    }

    @Test
    void successCancelBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("poby").build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012").build();
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(9000L)
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(CANCEL)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(CANCEL_AMOUNT)
                        .balanceSnapshot(10000L)
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        //when
        TransactionDto transactionDto = transactionService.cancelBalance("transactionId", "1000000000", CANCEL_AMOUNT);

        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(CANCEL_AMOUNT, captor.getValue().getAmount());
        assertEquals(10000L+CANCEL_AMOUNT, captor.getValue().getBalanceSnapshot());
        assertEquals(10000L, transactionDto.getBalanceSnapshot());
        assertEquals(CANCEL_AMOUNT, transactionDto.getAmount());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(CANCEL, transactionDto.getTransactionType());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 잔액 사용 취소 실패")
    void cancelTransaction_AccountNotFound(){
        //given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(Transaction.builder().build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1234567890",1000L));

        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }
    @Test
    @DisplayName("해당 거래 없음 - 계좌 해지 실패")
    void cancelTransaction_TransactionNotFound(){
        //given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1234567890",1000L));

        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("거래와 계좌가 매칭 실패 - 잔액 사용 취소 실패")
    void cancelTransaction_Transaction_AccountUnMatched(){
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("poby").build();
        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012").build();
        Account accountNotUse = Account.builder()
                .id(2L)
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000013").build();
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(9000L)
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(accountNotUse));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1000000010",CANCEL_AMOUNT));

        //then
        assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UNMATCHED, accountException.getErrorCode());
    }

    @Test
    @DisplayName("거래금액과 취소금액이 다름 - 잔액 사용 취소 실패")
    void cancelTransaction_CancleMustFully(){
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("poby").build();
        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012").build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT+1000L)
                .balanceSnapshot(9000L)
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1000000010",CANCEL_AMOUNT));

        //then
        assertEquals(ErrorCode.CANCEL_MUST_FULLY, accountException.getErrorCode());
    }

    @Test
    @DisplayName("취소는 1년까지만 가능 - 잔액 사용 취소 실패")
    void cancelTransaction_TooOld(){
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("poby").build();
        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012").build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now().minusYears(1))
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(9000L)
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1000000010",CANCEL_AMOUNT));

        //then
        assertEquals(ErrorCode.TOO_OLD_ORDER_TO_CANCEL, accountException.getErrorCode());
    }
    
    @Test
    void successQueryTransaction(){
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("poby").build();
        Account account = Account.builder()
                .id(1L)
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012").build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now().minusYears(1))
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(9000L)
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        //when
        TransactionDto transactionDto = transactionService.queryTransaction("trxId");
        //then
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(CANCEL_AMOUNT, transactionDto.getAmount());
        assertEquals("transactionId", transactionDto.getTransactionId());
    }

    @Test
    @DisplayName("해당 거래 없음 - 거래 조회 실패")
    void queryTransaction_TransactionNotFound(){
        //given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.queryTransaction("transactionId"));

        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, accountException.getErrorCode());
    }
}