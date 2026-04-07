package com.threlease.base.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 기반 분산 락 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * 락의 이름 (SpEL 지원)
     */
    String key();

    /**
     * 락 획득을 대기할 시간
     */
    long waitTime() default 5L;

    /**
     * 락을 획득한 후 유지할 시간 (이 시간이 지나면 자동 해제)
     */
    long leaseTime() default 3L;

    /**
     * 시간 단위
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
