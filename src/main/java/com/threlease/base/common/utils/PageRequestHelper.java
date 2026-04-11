package com.threlease.base.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 프론트엔드 요청을 Spring Data JPA Pageable로 변환하는 유틸리티
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageRequestHelper {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    /**
     * 기본 페이지네이션 생성 (0페이지, 10개씩)
     */
    public static Pageable of(Integer page, Integer size) {
        return of(page, size, Sort.unsorted());
    }

    /**
     * 정렬이 포함된 페이지네이션 생성
     */
    public static Pageable of(Integer page, Integer size, Sort sort) {
        int finalPage = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int finalSize = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        
        return PageRequest.of(finalPage, finalSize, sort != null ? sort : Sort.unsorted());
    }

    /**
     * 최신순 정렬(ID 내림차순)이 기본인 페이지네이션 생성
     */
    public static Pageable ofLatest(Integer page, Integer size) {
        return of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    }

    /**
     * 검색어 정규화: null/공백은 null로 반환하고, 과도하게 긴 입력은 제한합니다.
     */
    public static String searchQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String normalized = query.trim();
        return normalized.substring(0, Math.min(normalized.length(), 100));
    }
}
