package com.zerobase.Account.service;

import com.zerobase.Account.domain.Account;
import com.zerobase.Account.domain.AccountUser;
import com.zerobase.Account.dto.AccountDto;
import com.zerobase.Account.exception.AccountException;
import com.zerobase.Account.repository.AccountRepository;
import com.zerobase.Account.repository.AccountUserRepository;
import com.zerobase.Account.type.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.zerobase.Account.type.AccountStatus.IN_USE;
import static com.zerobase.Account.type.ErrorCode.*;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    /**
     * 사용자가 있는지 조회
     * 계좌 번호를 생성
     * 계좌 저장하고, 그 정보를 넘긴다.
     */
    @Override
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        AccountUser accountUser = getAccountUser(userId);

        // 유저가 있는지 확인
        validateCreateAccount(accountUser);

        // 랜덤 10자리 계좌 생성됨
        String newAccountNumber = getNewAccountNumber(accountUser);

        // 이미 있는 계좌 번호인지 확인
        validateAccountNumber(newAccountNumber);

        return AccountDto.fromEntity(
                accountRepository.save(
                        Account.builder()
                                .accountUser(accountUser)
                                .accountStatus(IN_USE)
                                .accountNumber(newAccountNumber)
                                .balance(initialBalance)
                                .registeredAt(LocalDateTime.now())
                                .build()
                )
        );
    }

    private String getNewAccountNumber(AccountUser accountUser) {
        return accountRepository.findFirstByAccountUserOrderByIdDesc(accountUser)
                .map(account -> (Long.parseLong(account.getAccountNumber())) + 1 + "")
                .orElse(getRandomAccountTenNumber());
    }

    private void validateAccountNumber(String newAccountNumber) {
        if(accountRepository.findByAccountNumber(newAccountNumber).isPresent()) {
            throw new AccountException(ACCOUNTNUMBER_ALREADY_USED);
        }
    }

    public String getRandomAccountTenNumber() {
        //accountNumber 자동 생성
        Random rand = new Random();
        // 첫 숫자 1에서 9사이로 생성
        String randomAccountTenNumber = Integer.toString(rand.nextInt(9) + 1);

        // 나머지 뒷 9숫자 생성
        for (int i = 0; i < 9; i++) {
            int digit = rand.nextInt(10);
            randomAccountTenNumber += Integer.toString(digit);
        }
        return randomAccountTenNumber;
    }

    @Override
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = getAccountUser(userId);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));
        validateDeleteAccount(account, accountUser);

        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnregisteredAt(LocalDateTime.now());

        accountRepository.save(account);
        return AccountDto.fromEntity(account);

    }

    @Override
    public void validateDeleteAccount(Account account, AccountUser accountUser) {
        if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UNMATCHED);
        }
        if (account.getAccountStatus() == AccountStatus.UNREGISTERED) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() > 0) {
            throw new AccountException(BALANCE_NOT_EMPTY);
        }
    }

    @Override
    public void validateCreateAccount(AccountUser accountUser) {
        if(accountRepository.countByAccountUser(accountUser)>=10){
            throw new AccountException(MAX_COUNT_FOR_USER_10);
        }
    }

    @Override
    @Transactional
    public Account findAccount(Long id) {
        return accountRepository.findById(id).get();
    }

    @Override
    @Transactional
    public List<AccountDto> getAccountByUserId(Long userId) {
        AccountUser accountUser = getAccountUser(userId);
        List<Account> accounts = accountRepository.findByAccountUser(accountUser);
        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(toList());
    }

    @Override
    public AccountUser getAccountUser(Long userId) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
        return accountUser;
    }
}
