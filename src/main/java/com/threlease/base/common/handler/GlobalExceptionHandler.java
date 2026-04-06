package com.threlease.base.common.handler;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.utils.responses.BasicResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BasicResponse<Void>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(
                BasicResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build()
        );
    }

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