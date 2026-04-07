package com.threlease.base.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 버전을 명시하는 어노테이션
 * 예: @ApiVersion(1) -> URL에 /api/v1 자동으로 붙음
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {
    int value() default 1;
}
