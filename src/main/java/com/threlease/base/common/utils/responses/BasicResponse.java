package com.threlease.base.common.utils.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.google.gson.Gson;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;
    private String correlationId;
    private String path;
    private LocalDateTime timestamp;
    private List<FieldValidationError> errors;

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * 성공 응답 객체 생성
     */
    public static <T> BasicResponse<T> success(T data) {
        return BasicResponse.<T>builder().success(true).code("OK").data(data).timestamp(LocalDateTime.now()).build();
    }

    public static <T> BasicResponse<T> success(T data, String message) {
        return BasicResponse.<T>builder().success(true).code("OK").data(data).message(message).timestamp(LocalDateTime.now()).build();
    }

    /**
     * 실패 응답 객체 생성 (GlobalExceptionHandler 용)
     */
    public static <T> BasicResponse<T> error(String message) {
        return BasicResponse.<T>builder().success(false).code("ERROR").message(message).timestamp(LocalDateTime.now()).build();
    }

    public static <T> BasicResponse<T> error(String code, String message, String path, String correlationId, List<FieldValidationError> errors) {
        return BasicResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .path(path)
                .correlationId(correlationId)
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }

    // --- ResponseEntity 팩토리 메서드들 ---

    public static <T> ResponseEntity<BasicResponse<T>> ok(T data) {
        return ResponseEntity.ok(success(data));
    }

    public static <T> ResponseEntity<BasicResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(success(data, message));
    }

    public static <T> ResponseEntity<BasicResponse<T>> created(T data) {
        return ResponseEntity.status(201).body(success(data));
    }

    public static <T> ResponseEntity<BasicResponse<T>> created(T data, String message) {
        return ResponseEntity.status(201).body(success(data, message));
    }

    public static <T> ResponseEntity<BasicResponse<T>> noContent() {
        return ResponseEntity.status(204).body(BasicResponse.<T>builder().success(true).code("NO_CONTENT").timestamp(LocalDateTime.now()).build());
    }

    public static <T> ResponseEntity<BasicResponse<T>> badRequest(String message) {
        return ResponseEntity.status(400).body(error(message));
    }

    public static <T> ResponseEntity<BasicResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(401).body(error(message));
    }

    public static <T> ResponseEntity<BasicResponse<T>> forbidden(String message) {
        return ResponseEntity.status(403).body(error(message));
    }

    public static <T> ResponseEntity<BasicResponse<T>> notFound(String message) {
        return ResponseEntity.status(404).body(error(message));
    }

    public static <T> ResponseEntity<BasicResponse<T>> conflict(String message) {
        return ResponseEntity.status(409).body(error(message));
    }

    public static <T> ResponseEntity<BasicResponse<T>> internalError(String message) {
        return ResponseEntity.status(500).body(error(message));
    }

    @Getter
    @Builder
    public static class FieldValidationError {
        private String field;
        private String message;
    }
}
