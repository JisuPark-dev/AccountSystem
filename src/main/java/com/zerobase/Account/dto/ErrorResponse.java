package com.zerobase.Account.dto;

import com.zerobase.Account.type.ErrorCode;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
    private ErrorCode errorCode;
    private String errorMessage;

}
