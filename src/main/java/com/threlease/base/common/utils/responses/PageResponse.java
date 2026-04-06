package com.threlease.base.common.utils.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Function;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    private boolean success;
    private String message;

    private List<T> data;
    private long totalElements;
    private int totalPages;
    private int currentPage;   // 0-based
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    // 2xx
    public static <T> ResponseEntity<PageResponse<T>> ok(Page<T> page) {
        return ResponseEntity.status(200).body(from(page));
    }

    public static <T, R> ResponseEntity<PageResponse<R>> ok(Page<T> page, Function<T, R> mapper) {
        return ResponseEntity.status(200).body(from(page, mapper));
    }

    // 4xx
    public static <T> ResponseEntity<PageResponse<T>> badRequest(String message) {
        return ResponseEntity.status(400).body(error(message));
    }

    public static <T> ResponseEntity<PageResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(401).body(error(message));
    }

    public static <T> ResponseEntity<PageResponse<T>> forbidden(String message) {
        return ResponseEntity.status(403).body(error(message));
    }

    public static <T> ResponseEntity<PageResponse<T>> notFound(String message) {
        return ResponseEntity.status(404).body(error(message));
    }

    public static <T> ResponseEntity<PageResponse<T>> conflict(String message) {
        return ResponseEntity.status(409).body(error(message));
    }

    // 5xx
    public static <T> ResponseEntity<PageResponse<T>> internalError() {
        return ResponseEntity.status(500).body(error("서버 내부 오류가 발생했습니다."));
    }

    public static <T> ResponseEntity<PageResponse<T>> internalError(String message) {
        return ResponseEntity.status(500).body(error(message));
    }

    // 내부 빌더 헬퍼
    private static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .success(true)
                .data(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private static <T, R> PageResponse<R> from(Page<T> page, Function<T, R> mapper) {
        return PageResponse.<R>builder()
                .success(true)
                .data(page.getContent().stream().map(mapper).toList())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    // 에러 응답
    public static <T> PageResponse<T> error(String message) {
        return PageResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}