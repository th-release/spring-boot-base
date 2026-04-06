package com.threlease.base.functions.auth;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.provider.JwtProvider;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.dto.TokenResponseDto;
import com.threlease.base.repositories.auth.AuthRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;
    private final ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider;
    
    // Redis 미사용 시 로컬 메모리 저장소
    private final Map<String, String> localRefreshTokenStorage = new ConcurrentHashMap<>();

    public AuthService(AuthRepository authRepository, 
                       JwtProvider jwtProvider, 
                       ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider) {
        this.authRepository = authRepository;
        this.jwtProvider = jwtProvider;
        this.stringRedisTemplateProvider = stringRedisTemplateProvider;
    }

    @Cacheable(value = "user", key = "#uuid", unless = "#result == null")
    public Optional<AuthEntity> findOneByUUID(String uuid) {
        return authRepository.findOneByUUID(uuid);
    }

    @Cacheable(value = "user", key = "#username", unless = "#result == null")
    public Optional<AuthEntity> findOneByUsername(String username) {
        return authRepository.findOneByUsername(username);
    }

    @Caching(evict = {
            @CacheEvict(value = "user", key = "#auth.uuid"),
            @CacheEvict(value = "user", key = "#auth.username")
    })
    public void authSave(AuthEntity auth) {
        authRepository.save(auth);
    }

    /**
     * 로그인 - 토큰 세트 발급
     */
    public TokenResponseDto issueTokens(AuthEntity user) {
        String accessToken = jwtProvider.createAccessToken(user.getUuid());
        String refreshToken = jwtProvider.createRefreshToken(user.getUuid());

        saveRefreshToken(user.getUuid(), refreshToken);

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 토큰 갱신 (Token Rotation 적용)
     */
    public TokenResponseDto refresh(String refreshToken) {
        String uuid = jwtProvider.getSubject(refreshToken);
        if (uuid == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        String storedToken = getStoredRefreshToken(uuid);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            // 토큰 탈취 가능성 (이전 토큰 재사용 시도) -> 저장된 토큰 삭제 후 에러
            deleteRefreshToken(uuid);
            log.warn("Refresh token reuse detected for user: {}. Revoking all tokens.", uuid);
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        AuthEntity user = findOneByUUID(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 새로운 토큰 세트 발급 (Rotation)
        return issueTokens(user);
    }

    private void saveRefreshToken(String uuid, String refreshToken) {
        StringRedisTemplate redisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set("refresh_token:" + uuid, refreshToken, 14, TimeUnit.DAYS);
        } else {
            localRefreshTokenStorage.put(uuid, refreshToken);
        }
    }

    private String getStoredRefreshToken(String uuid) {
        StringRedisTemplate redisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().get("refresh_token:" + uuid);
        } else {
            return localRefreshTokenStorage.get(uuid);
        }
    }

    private void deleteRefreshToken(String uuid) {
        StringRedisTemplate redisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.delete("refresh_token:" + uuid);
        } else {
            localRefreshTokenStorage.remove(uuid);
        }
    }

    public Optional<AuthEntity> findOneByToken(String token) {
        return jwtProvider.findOneByToken(token);
    }
}
