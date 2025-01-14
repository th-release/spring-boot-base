package com.threlease.base.common.exception;

public class TokenValidException extends RuntimeException {
    // 생성자 정의
    public TokenValidException(String message) {
        super(message);
    }

    public TokenValidException(String message, Throwable cause) {
        super(message, cause);
    }
}