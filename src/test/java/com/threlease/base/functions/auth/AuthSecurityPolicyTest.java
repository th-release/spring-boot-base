package com.threlease.base.functions.auth;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.properties.app.auth.AuthSecurityProperties;
import com.threlease.base.common.properties.app.jwt.JwtProperties;
import com.threlease.base.common.properties.app.redis.RedisProperties;
import com.threlease.base.common.properties.app.token.TokenProperties;
import com.threlease.base.common.provider.JwtProvider;
import com.threlease.base.common.utils.crypto.HashComponent;
import com.threlease.base.common.utils.random.RandomComponent;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.repositories.auth.AuthRepository;
import com.threlease.base.repositories.auth.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthSecurityPolicyTest {
    private AuthService authService;

    @BeforeEach
    void setUp() {
        AuthRepository authRepository = mock(AuthRepository.class);
        RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
        HashComponent hashComponent = new HashComponent();
        RandomComponent randomComponent = new RandomComponent();

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("Nwzbu8o3Rkf0iOJj0wpY2i749zjM7kr6Hnnl6x/n4e+tJoAmn5wYJt/jeFX71cawaR4kQFTw1ACeJgsHAJ/AeA==");
        jwtProperties.validateSecretKey();
        JwtProvider jwtProvider = new JwtProvider(authRepository, jwtProperties);

        RedisProperties redisProperties = new RedisProperties();
        redisProperties.setEnabled(false);

        TokenProperties tokenProperties = new TokenProperties();
        tokenProperties.setStorage("rdb");

        AuthSecurityProperties authSecurityProperties = new AuthSecurityProperties();
        authSecurityProperties.getLoginFailure().setEnabled(true);
        authSecurityProperties.getLoginFailure().setMaxAttempts(3);
        authSecurityProperties.getLoginFailure().setLockMinutes(10);

        ObjectProvider<StringRedisTemplate> objectProvider = mock(ObjectProvider.class);
        when(objectProvider.getIfAvailable()).thenReturn(null);

        authService = new AuthService(
                authRepository,
                refreshTokenRepository,
                jwtProvider,
                objectProvider,
                hashComponent,
                randomComponent,
                redisProperties,
                tokenProperties,
                authSecurityProperties
        );
    }

    @Test
    void failedLoginLocksAccountAtThreshold() {
        AuthEntity user = AuthEntity.builder()
                .uuid("user-1")
                .username("tester")
                .nickname("tester")
                .password("encoded")
                .salt("salt")
                .build();

        authService.recordFailedLogin(user);
        authService.recordFailedLogin(user);
        authService.recordFailedLogin(user);

        assertEquals(3, user.getFailedLoginCount());
        assertThrows(BusinessException.class, () -> authService.ensureLoginAllowed(user));
    }

    @Test
    void successfulLoginResetsLockState() {
        AuthEntity user = AuthEntity.builder()
                .uuid("user-1")
                .username("tester")
                .nickname("tester")
                .password("encoded")
                .salt("salt")
                .failedLoginCount(3)
                .lockedUntil(LocalDateTime.now().plusMinutes(5))
                .build();

        authService.recordSuccessfulLogin(user, "127.0.0.1");

        assertEquals(0, user.getFailedLoginCount());
        assertEquals("127.0.0.1", user.getLastLoginIp());
    }
}
