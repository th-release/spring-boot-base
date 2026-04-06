package com.threlease.base.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 공통
    INVALID_INPUT(400, "잘못된 입력입니다."),
    INTERNAL_ERROR(500, "서버 내부 오류가 발생했습니다."),

    // 인증 토큰
    TOKEN_MISSING(400, "Authorization 헤더가 없습니다."),
    TOKEN_INVALID(401, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(401, "만료된 토큰입니다."),

    // 유저
    USER_NOT_FOUND(404, "해당 유저를 찾을 수 없습니다."),
    USER_DUPLICATE(409, "이미 사용 중인 아이디입니다."),
    WRONG_PASSWORD(403, "아이디 혹은 비밀번호를 확인해주세요."),

    // 권한
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "접근 권한이 없습니다.");

    private final int status;
    private final String message;
}
