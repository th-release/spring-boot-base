package com.threlease.base.common.handler;

import com.threlease.base.common.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 분산 락 AOP 핸들러
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RedissonClient.class)
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private final LockTransactionHandler lockTransactionHandler;

    @Around("@annotation(distributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 1. SpEL 파싱을 통한 락 키 생성
        String key = getDynamicKey(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        String lockKey = "lock:" + method.getName() + ":" + key;

        RLock rLock = redissonClient.getLock(lockKey);

        try {
            // 2. 락 획득 시도
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                log.warn("Failed to acquire lock for key: {}", lockKey);
                return null;
            }

            log.debug("Acquired lock for key: {}", lockKey);
            // 3. 락 획득 상태에서 트랜잭션 처리
            return lockTransactionHandler.proceed(joinPoint);
        } catch (InterruptedException e) {
            throw new RuntimeException("Lock acquisition interrupted", e);
        } finally {
            try {
                // 4. 락 해제
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                    log.debug("Released lock for key: {}", lockKey);
                }
            } catch (IllegalMonitorStateException e) {
                log.warn("Lock already released: {}", lockKey);
            }
        }
    }

    private String getDynamicKey(String[] parameterNames, Object[] args, String key) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        Object value = parser.parseExpression(key).getValue(context, Object.class);
        return value != null ? value.toString() : "";
    }
}
