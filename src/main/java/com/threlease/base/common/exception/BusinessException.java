package com.threlease.base.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 동적 메시지가 필요할 때: new BusinessException(ErrorCode.USER_NOT_FOUND, "해당 유저(id=123)를 찾을 수 없습니다.")
    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }
}
