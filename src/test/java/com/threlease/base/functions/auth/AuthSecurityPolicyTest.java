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
import com.threlease.base.entities.AuthLoginHistoryEntity;
import com.threlease.base.repositories.auth.AuthRepository;
import com.threlease.base.repositories.auth.AuthLoginHistoryRepository;
import com.threlease.base.repositories.auth.AuthMfaRepository;
import com.threlease.base.repositories.auth.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthSecurityPolicyTest {
    private AuthService authService;
    private final List<AuthLoginHistoryEntity> loginHistories = new ArrayList<>();

    @BeforeEach
    void setUp() {
        AuthRepository authRepository = mock(AuthRepository.class);
        AuthLoginHistoryRepository authLoginHistoryRepository = mock(AuthLoginHistoryRepository.class);
        AuthMfaRepository authMfaRepository = mock(AuthMfaRepository.class);
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
        when(authLoginHistoryRepository.save(any(AuthLoginHistoryEntity.class))).thenAnswer(invocation -> {
            AuthLoginHistoryEntity history = invocation.getArgument(0);
            loginHistories.add(0, history); // Add to beginning to simulate DESC order
            return history;
        });
        when(authLoginHistoryRepository.findRecentByUser(any(AuthEntity.class), any())).thenAnswer(invocation -> new PageImpl<>(loginHistories));
        when(authLoginHistoryRepository.findLatestSuccessfulByUser(any(AuthEntity.class), any())).thenAnswer(invocation -> 
            new PageImpl<>(loginHistories.stream().filter(AuthLoginHistoryEntity::isSuccess).toList()));

        authService = new AuthService(
                authRepository,
                authLoginHistoryRepository,
                authMfaRepository,
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

        assertEquals(3, authService.getFailedLoginCount(user));
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
                .build();

        authService.recordSuccessfulLogin(user, "127.0.0.1");

        assertEquals(0, authService.getFailedLoginCount(user));
        assertEquals("127.0.0.1", authService.getLastLoginIp(user));
    }

}
