package com.zerobase.Account.service;

import com.zerobase.Account.domain.Account;
import com.zerobase.Account.domain.AccountUser;
import com.zerobase.Account.domain.Transaction;
import com.zerobase.Account.dto.TransactionDto;
import com.zerobase.Account.type.TransactionResultType;
import com.zerobase.Account.type.TransactionType;

public interface TransactionService {

    TransactionDto useBalance(Long userId, String accountNumber, Long amount);

    void validateUseBalance(AccountUser user, Account account, Long amount);


    void saveFailedUseTransaction(String accountNumber, Long amount);

    public Transaction saveAndGetTransaction(
            TransactionType transactionType,
            TransactionResultType transactionResultType,
            Account account,
            Long amount);

    TransactionDto cancelBalance(
            String transactionId,
            String accountNumber,
            Long amount
    );

    void validateCancelBalance(Long amount, Transaction transaction, Account account);

    void saveFailedCancelTransaction(String accountNumber, Long amount);

    TransactionDto queryTransaction(String transactionId);
}
