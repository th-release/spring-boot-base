package com.threlease.base.common.handler;

import com.threlease.base.common.exception.TokenValidException;
import com.threlease.base.common.utils.responses.BasicResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // @Valid 검증 실패 - 필드별 메시지를 줄바꿈으로 합쳐서 반환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BasicResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("\n"));

        return ResponseEntity.badRequest().body(
                BasicResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .build()
        );
    }

    // TokenInterceptor에서 던지는 토큰 검증 실패
    // 메시지 형식: "상태코드:메시지" (예: "403:Invalid Token")
    @ExceptionHandler(TokenValidException.class)
    public ResponseEntity<BasicResponse<Void>> handleTokenValidException(TokenValidException ex) {
        String[] parts = ex.getMessage().split(":", 2);
        int status = 401;
        String message = ex.getMessage();

        try {
            status = Integer.parseInt(parts[0].trim());
            message = parts.length > 1 ? parts[1].trim() : message;
        } catch (NumberFormatException ignored) {}

        return ResponseEntity.status(status).body(
                BasicResponse.<Void>builder()
                        .success(false)
                        .message(message)
                        .build()
        );
    }

    // 처리되지 않은 예외 - 500 반환 및 서버 로그 기록
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BasicResponse<Void>> handleException(Exception ex) {
        return ResponseEntity.status(500).body(
                BasicResponse.<Void>builder()
                        .success(false)
                        .message("서버 내부 오류가 발생했습니다.")
                        .build()
        );
    }
}
