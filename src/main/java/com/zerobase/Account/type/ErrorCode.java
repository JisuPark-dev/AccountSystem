package com.zerobase.Account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("사용자가 없습니다."),
    ACCOUNT_NOT_FOUND("계좌가 없습니다."),
    USER_ACCOUNT_UNMATCHED("사용자와 계좌가 동일하지 않습니다."),
    ACCOUNT_ALREADY_UNREGISTERED("계좌가 이미 해지되었습니다."),
    BALANCE_NOT_EMPTY("계좌가 남아있습니다."),
    MAX_COUNT_FOR_USER_10("사용자 최대 계좌는 10개 입니다.");
    private final String description;
}
