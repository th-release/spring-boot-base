package com.threlease.base.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 공통
    INVALID_INPUT(400, "잘못된 입력입니다."),
    INTERNAL_ERROR(500, "서버 내부 오류가 발생했습니다."),
    VALIDATION_FAILED(400, "입력값 검증에 실패했습니다."),

    // 인증 토큰
    TOKEN_MISSING(400, "Authorization 헤더가 없습니다."),
    TOKEN_INVALID(401, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(401, "만료된 토큰입니다."),

    // 유저
    USER_NOT_FOUND(404, "해당 유저를 찾을 수 없습니다."),
    USER_DUPLICATE(409, "이미 사용 중인 아이디입니다."),
    WRONG_PASSWORD(403, "아이디 혹은 비밀번호를 확인해주세요."),
    PASSWORD_CHANGE_MISMATCH(400, "새 비밀번호 확인 값이 일치하지 않습니다."),
    PASSWORD_RESET_CODE_INVALID(400, "비밀번호 재설정 인증 코드가 유효하지 않습니다."),
    PASSWORD_RESET_CODE_EXPIRED(400, "비밀번호 재설정 인증 코드가 만료되었습니다."),
    ACCOUNT_LOCKED(423, "로그인이 잠금 상태입니다. 잠시 후 다시 시도해주세요."),
    ACCOUNT_INACTIVE(403, "현재 사용할 수 없는 계정 상태입니다."),
    EMAIL_DISABLED(400, "이메일 기능이 비활성화되어 있습니다."),
    EMAIL_NOT_REGISTERED(400, "해당 계정에 등록된 이메일이 없습니다."),
    FIREBASE_DISABLED(400, "Firebase 기능이 비활성화되어 있습니다."),
    MFA_DISABLED(400, "MFA 기능이 비활성화되어 있습니다."),
    MFA_REQUIRED(401, "MFA 인증 코드가 필요합니다."),
    MFA_INVALID_CODE(401, "유효하지 않은 MFA 인증 코드입니다."),
    MFA_NOT_CONFIGURED(400, "MFA가 설정되지 않았습니다."),

    // 권한
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),

    // Rate Limit
    TOO_MANY_REQUESTS(429, "너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해 주세요."),

    // 파일
    FILE_UPLOAD_INVALID(400, "허용되지 않은 파일 업로드 요청입니다."),
    FILE_NOT_FOUND(404, "해당 파일을 찾을 수 없습니다.");

    private final int status;
    private final String message;
}
