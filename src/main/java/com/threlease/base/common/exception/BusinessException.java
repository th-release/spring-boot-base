package com.threlease.base.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object[] args;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, (Object[]) null);
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * @deprecated 다국어 처리를 위해 getMessage()를 권장합니다.
     * 직접 메시지를 지정하고 싶을 때만 사용하세요.
     */
    @Deprecated
    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
        this.args = null;
    }
}
