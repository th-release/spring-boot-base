package com.threlease.base.common.handler;

import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 분산 락 획득 후 새로운 트랜잭션을 시작하기 위한 핸들러
 */
@Component
@ConditionalOnBean(RedissonClient.class)
public class LockTransactionHandler {

    /**
     * 락 획득 후 트랜잭션 보장을 위해 REQUIRES_NEW로 별도 트랜잭션 실행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
