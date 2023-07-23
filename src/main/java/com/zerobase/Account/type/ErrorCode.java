package com.zerobase.Account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다"),
    INVALID_REQUEST("잘못된 요청입니다."),
    USER_NOT_FOUND("사용자가 없습니다."),
    ACCOUNT_NOT_FOUND("계좌가 없습니다."),
    ACCOUNTNUMBER_ALREADY_USED("이미 사용중인 계좌번호입니다."),
    ACCOUNT_TRANSACTION_LOCK("해당계좌는 사용중입니다."),
    TRANSACTION_NOT_FOUND("계좌가 없습니다."),
    AMOUNT_EXCEED_BALANCE("잔액이 부족합니다."),
    TRANSACTION_ACCOUNT_UNMATCHED("거래와 계좌가 동일하지 않습니다."),
    CANCEL_MUST_FULLY("부분취소는 허용되지 않습니다."),
    TOO_OLD_ORDER_TO_CANCEL("1년이 지난 거래의 취소는 불가능합니다."),
    USER_ACCOUNT_UNMATCHED("사용자와 계좌가 동일하지 않습니다."),
    ACCOUNT_ALREADY_UNREGISTERED("계좌가 이미 해지되었습니다."),
    BALANCE_NOT_EMPTY("계좌가 남아있습니다."),
    MAX_COUNT_FOR_USER_10("사용자 최대 계좌는 10개 입니다.");
    private final String description;
}
