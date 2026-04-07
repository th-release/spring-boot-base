package com.threlease.base.common.utils.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Querydsl 동적 쿼리 생성을 돕는 유틸리티 클래스
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuerydslUtils {

    /**
     * 문자열 필드에 대해 null/empty 체크 후 eq 조건 생성
     */
    public static BooleanExpression eqString(StringPath path, String value) {
        return StringUtils.hasText(value) ? path.eq(value) : null;
    }

    /**
     * 문자열 필드에 대해 null/empty 체크 후 contains 조건 생성 (LIKE %value%)
     */
    public static BooleanExpression contains(StringPath path, String value) {
        return StringUtils.hasText(value) ? path.contains(value) : null;
    }

    /**
     * 숫자 필드에 대해 null 체크 후 eq 조건 생성
     */
    public static <T extends Number & Comparable<?>> BooleanExpression eqNumber(NumberPath<T> path, T value) {
        return value != null ? path.eq(value) : null;
    }

    /**
     * 리스트가 비어있지 않으면 IN 조건 생성
     */
    public static <T> BooleanExpression in(com.querydsl.core.types.dsl.SimpleExpression<T> path, List<T> values) {
        return values != null && !values.isEmpty() ? path.in(values) : null;
    }

    /**
     * 여러 개의 BooleanExpression을 AND 조건으로 결합 (null 제외)
     */
    public static BooleanExpression andAll(BooleanExpression... expressions) {
        BooleanExpression result = null;
        for (BooleanExpression expression : expressions) {
            if (expression != null) {
                result = (result == null) ? expression : result.and(expression);
            }
        }
        return result;
    }
}
