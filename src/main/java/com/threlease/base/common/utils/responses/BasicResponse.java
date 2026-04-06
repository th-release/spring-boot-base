package com.threlease.base.common.utils.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import com.google.gson.Gson;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public String toJson() {
        Gson gson = new Gson();

        return gson.toJson(this);
    }

    // 2xx
    public static <T> ResponseEntity<BasicResponse<T>> ok(T data) {
        return ResponseEntity.status(200).body(
                BasicResponse.<T>builder().success(true).data(data).build()
        );
    }

    public static <T> ResponseEntity<BasicResponse<T>> ok(T data, String message) {
        return ResponseEntity.status(200).body(
                BasicResponse.<T>builder().success(true).data(data).message(message).build()
        );
    }

    public static <T> ResponseEntity<BasicResponse<T>> created(T data) {
        return ResponseEntity.status(201).body(
                BasicResponse.<T>builder().success(true).data(data).build()
        );
    }

    public static <T> ResponseEntity<BasicResponse<T>> created(T data, String message) {
        return ResponseEntity.status(201).body(
                BasicResponse.<T>builder().success(true).data(data).message(message).build()
        );
    }

    public static <T> ResponseEntity<BasicResponse<T>> noContent() {
        return ResponseEntity.status(204).body(
                BasicResponse.<T>builder().success(true).build()
        );
    }

    // 4xx
    public static <T> ResponseEntity<BasicResponse<T>> badRequest(String message) {
        return ResponseEntity.status(400).body(
                BasicResponse.<T>builder().success(false).message(message).build()
        );
    }

    public static <T> ResponseEntity<BasicResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(401).body(
                BasicResponse.<T>builder().success(false).message(message).build()
        );
    }

    public static <T> ResponseEntity<BasicResponse<T>> forbidden(String message) {
        return ResponseEntity.status(403).body(
                BasicResponse.<T>builder().success(false).message(message).build()
        );
    }

    public static <T> ResponseEntity<BasicResponse<T>> notFound(String message) {
        return ResponseEntity.status(404).body(
                BasicResponse.<T>builder().success(false).message(message).build()
        );
    }

    public static <T> ResponseEntity<BasicResponse<T>> conflict(String message) {
        return ResponseEntity.status(409).body(
                BasicResponse.<T>builder().success(false).message(message).build()
        );
    }

    // 5xx
    public static <T> ResponseEntity<BasicResponse<T>> internalError(String message) {
        return ResponseEntity.status(500).body(
                BasicResponse.<T>builder().success(false).message(message).build()
        );
    }

    public static <T> ResponseEntity<BasicResponse<T>> internalError() {
        return ResponseEntity.status(500).body(
                BasicResponse.<T>builder().success(false).message("서버 내부 오류가 발생했습니다.").build()
        );
    }
}
