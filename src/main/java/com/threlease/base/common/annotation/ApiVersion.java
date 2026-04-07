package com.threlease.base.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 버전을 명시하기 위한 어노테이션
 * 클래스나 메서드에 적용 가능하며, URL 경로에 버전을 추가합니다.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {
    int[] value() default {1};
}
