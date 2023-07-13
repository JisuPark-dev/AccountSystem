package com.zerobase.Account.dto;

import com.zerobase.Account.domain.Account;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountDto {
    private Long userId;
    private String accountNumber;
    private Long balance;
    private LocalDateTime registerAt;
    private LocalDateTime unRegisterAt;

    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
                .userId(account.getAccountUser().getId())
                .accountNumber(account.getAccountNumber())
                .registerAt(account.getRegisteredAt())
                .balance(account.getBalance())
                .unRegisterAt(account.getUnregisteredAt())
                .build();
    }

}
