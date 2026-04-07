package com.threlease.base.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 엑셀 컬럼 매핑을 위한 어노테이션
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {
    /**
     * 엑셀 헤더에 표시될 이름
     */
    String headerName();

    /**
     * 컬럼 순서 (0부터 시작)
     */
    int order() default 999;
}
