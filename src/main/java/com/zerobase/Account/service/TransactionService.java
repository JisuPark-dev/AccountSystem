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
import com.zerobase.Account.type.TransactionResultType;
import com.zerobase.Account.type.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.zerobase.Account.type.TransactionResultType.F;
import static com.zerobase.Account.type.TransactionResultType.S;
import static com.zerobase.Account.type.TransactionType.USE;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public TransactionDto useBalance(Long userId, String accountNumber, Long amount) {
        AccountUser user = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateUseBalance(user, account, amount);

        account.useBalance(amount);

        return TransactionDto.fromEntity(
                saveAndGetTransaction(S, account, amount)
        );
    }

    private void validateUseBalance(AccountUser user, Account account, Long amount) {
        if(!Objects.equals(user.getId(), account.getAccountUser().getId())){
            throw new AccountException(ErrorCode.USER_ACCOUNT_UNMATCHED);
        }
        if (account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new AccountException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (amount > account.getBalance()) {
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }

    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveAndGetTransaction(F, account, amount);
    }

    private Transaction saveAndGetTransaction(
            TransactionResultType transactionResultType,
            Account account,
            Long amount) {
        return transactionRepository.save(
                Transaction.builder()
                        .transactionType(USE)
                        .transactionResultType(transactionResultType)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build()
        );
    }
}
