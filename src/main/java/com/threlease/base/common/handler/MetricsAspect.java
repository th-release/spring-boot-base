package com.threlease.base.common.handler;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * API 성능 측정을 위한 커스텀 메트릭 AOP
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MetricsAspect {

    private final MeterRegistry meterRegistry;

    @Around("execution(* com.threlease.base.functions..*Controller.*(..))")
    public Object measureApiPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        Timer timer = Timer.builder("api.performance")
                .description("API Execution Performance")
                .tag("method", method)
                .tag("uri", uri)
                .tag("class", className)
                .tag("handler", methodName)
                .register(meterRegistry);

        return timer.recordCallable(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                // 예외 발생 시에도 메트릭 기록 후 던짐
                throw new RuntimeException(e);
            }
        });
    }
}
