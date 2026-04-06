package com.threlease.base.common.handler;

import com.threlease.base.common.annotation.RateLimit;
import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private final ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider;
    private final Map<String, Long> localCache = new ConcurrentHashMap<>();
    private final Map<String, Long> localExpiration = new ConcurrentHashMap<>();

    public RateLimitAspect(ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider) {
        this.stringRedisTemplateProvider = stringRedisTemplateProvider;
    }

    @Before("@annotation(rateLimit)")
    public void rateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        String identifier = getIdentifier(request);
        String key = String.format("rate_limit:%s:%s", joinPoint.getSignature().toShortString(), identifier);

        StringRedisTemplate redisTemplate = stringRedisTemplateProvider.getIfAvailable();
        long count;

        if (redisTemplate != null) {
            Long redisCount = redisTemplate.opsForValue().increment(key);
            if (redisCount != null && redisCount == 1) {
                redisTemplate.expire(key, rateLimit.window(), TimeUnit.SECONDS);
            }
            count = redisCount != null ? redisCount : 0;
        } else {
            count = handleLocalRateLimit(key, rateLimit.window());
        }

        if (count > rateLimit.limit()) {
            log.warn("Rate limit exceeded for key: {} (count: {})", key, count);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    private long handleLocalRateLimit(String key, int windowSeconds) {
        long now = Instant.now().getEpochSecond();
        Long expiration = localExpiration.get(key);

        if (expiration == null || now > expiration) {
            localCache.put(key, 1L);
            localExpiration.put(key, now + windowSeconds);
            return 1L;
        }

        long count = localCache.getOrDefault(key, 0L) + 1;
        localCache.put(key, count);
        return count;
    }

    private String getIdentifier(HttpServletRequest request) {
        // 1. Spring Security 인증 정보 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }

        // 2. IP 주소 확인 (프록시 고려)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
