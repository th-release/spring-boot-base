package com.threlease.base.common.handler;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.utils.MessageUtils;
import com.threlease.base.common.utils.responses.BasicResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 * MessageUtils를 사용하여 에러 메시지를 다국어 처리합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 또는 @Validated 유효성 검사 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BasicResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("\n"));

        log.warn("Validation Error: {}", message);
        return ResponseEntity.badRequest().body(BasicResponse.error(message));
    }

    /**
     * 비즈니스 로직 예외 처리
     * ErrorCode의 이름을 키로 사용하여 다국어 메시지를 가져옵니다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BasicResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        
        // ErrorCode의 name() (예: USER_NOT_FOUND)으로 메시지 조회
        // 메시지가 없으면 ErrorCode의 기본 메시지 사용
        String localizedMessage = MessageUtils.getMessage(errorCode.name(), ex.getArgs());
        if (localizedMessage.equals(errorCode.name())) {
            localizedMessage = errorCode.getMessage();
        }

        log.warn("Business Exception [{}]: {}", errorCode.name(), localizedMessage);
        return ResponseEntity.status(errorCode.getStatus()).body(BasicResponse.error(localizedMessage));
    }

    /**
     * 그 외 정의되지 않은 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BasicResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled Exception: ", ex);
        
        // "common.error" 키로 다국어 메시지 조회, 없으면 기본 메시지 반환
        String message = MessageUtils.getMessage("common.error");
        if (message.equals("common.error")) {
            message = "서버 내부 오류가 발생했습니다.";
        }
        
        return ResponseEntity.status(500).body(BasicResponse.error(message));
    }
}
