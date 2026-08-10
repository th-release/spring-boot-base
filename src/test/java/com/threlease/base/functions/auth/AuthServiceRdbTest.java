package com.threlease.base.functions.auth;

import com.threlease.base.common.properties.app.auth.AuthSecurityProperties;
import com.threlease.base.common.properties.app.jwt.JwtProperties;
import com.threlease.base.common.properties.app.redis.RedisProperties;
import com.threlease.base.common.properties.app.token.TokenProperties;
import com.threlease.base.common.provider.JwtProvider;
import com.threlease.base.common.utils.crypto.HashComponent;
import com.threlease.base.common.utils.random.RandomComponent;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.RefreshTokenEntity;
import com.threlease.base.functions.auth.dto.RefreshTokenSessionDto;
import com.threlease.base.functions.auth.dto.TokenResponseDto;
import com.threlease.base.repositories.auth.AuthRepository;
import com.threlease.base.repositories.auth.AuthLoginHistoryRepository;
import com.threlease.base.repositories.auth.AuthMfaRepository;
import com.threlease.base.repositories.auth.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceRdbTest {
    private final AuthRepository authRepository = mock(AuthRepository.class);
    private final AuthLoginHistoryRepository authLoginHistoryRepository = mock(AuthLoginHistoryRepository.class);
    private final AuthMfaRepository authMfaRepository = mock(AuthMfaRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final HashComponent hashComponent = new HashComponent();
    private final RandomComponent randomComponent = new RandomComponent();
    private AuthUserService authUserService;
    private AuthSessionService authSessionService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("Nwzbu8o3Rkf0iOJj0wpY2i749zjM7kr6Hnnl6x/n4e+tJoAmn5wYJt/jeFX71cawaR4kQFTw1ACeJgsHAJ/AeA==");
        jwtProperties.validateSecretKey();

        JwtProvider jwtProvider = new JwtProvider(authRepository, jwtProperties);

        RedisProperties redisProperties = new RedisProperties();
        redisProperties.setEnabled(false);

        TokenProperties tokenProperties = new TokenProperties();
        tokenProperties.setStorage("rdb");
        tokenProperties.setMaxSessionsPerUser(5);

        AuthSecurityProperties authSecurityProperties = new AuthSecurityProperties();

        ObjectProvider<StringRedisTemplate> objectProvider = mock(ObjectProvider.class);
        when(objectProvider.getIfAvailable()).thenReturn(null);

        authUserService = new AuthUserService(
                authRepository,
                authLoginHistoryRepository,
                authMfaRepository,
                hashComponent,
                randomComponent,
                authSecurityProperties
        );

        authSessionService = new AuthSessionService(
                refreshTokenRepository,
                jwtProvider,
                objectProvider,
                hashComponent,
                redisProperties,
                tokenProperties,
                authUserService
        );

        authService = new AuthService(authUserService, authSessionService);
    }

    @Test
    void issueTokensAndGetSessionsWorkInRdbMode() {
        AuthEntity user = AuthEntity.builder()
                .uuid("user-1")
                .username("tester")
                .nickname("tester")
                .password("encoded")
                .build();

        when(refreshTokenRepository.findAllByUserAndRevokedFalse(any(AuthEntity.class))).thenReturn(List.of());
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TokenResponseDto tokenResponse = authService.issueTokens(user);

        assertNotNull(tokenResponse.getAccessToken());
        assertNotNull(tokenResponse.getRefreshToken());
    }

    @Test
    void logoutRevokesCurrentAccessTokenFamily() {
        AuthEntity user = AuthEntity.builder()
                .uuid("user-1")
                .username("tester")
                .nickname("tester")
                .password("encoded")
                .build();

        when(authRepository.findOneByUUID("user-1")).thenReturn(Optional.of(user));

        List<RefreshTokenEntity> savedTokens = new java.util.ArrayList<>();
        when(refreshTokenRepository.findAllByUserAndRevokedFalse(any(AuthEntity.class))).thenAnswer(invocation -> savedTokens.stream()
                .filter(token -> !token.isRevoked())
                .toList());
        when(refreshTokenRepository.findAllByFamilyId("family-1")).thenAnswer(invocation -> savedTokens.stream()
                .filter(token -> "family-1".equals(token.getFamilyId()))
                .toList());
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> {
            RefreshTokenEntity token = invocation.getArgument(0);
            savedTokens.removeIf(existing -> existing.getTokenId().equals(token.getTokenId()));
            savedTokens.add(token);
            return token;
        });

        TokenResponseDto tokenResponse = authService.issueTokens(user, "family-1");

        assertNotNull(authService.findOneByToken(tokenResponse.getAccessToken()).orElse(null));

        authService.logout(tokenResponse.getRefreshToken(), user.getUuid());

        assertTrue(authService.findOneByToken(tokenResponse.getAccessToken()).isEmpty());
    }

    @Test
    void logoutAllRevokesActiveSessionsInRdbMode() {
        RefreshTokenEntity token1 = RefreshTokenEntity.builder()
                .uuid("refresh-token-1")
                .user(AuthEntity.builder().uuid("user-1").build())
                .tokenId("token-1")
                .familyId("family-1")
                .tokenHash("hash-1")
                .expiryDate(java.time.LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        when(authRepository.findOneByUUID("user-1")).thenReturn(Optional.of(AuthEntity.builder().uuid("user-1").build()));
        when(refreshTokenRepository.findAllByUserAndRevokedFalse(any(AuthEntity.class))).thenReturn(List.of(token1));
        when(refreshTokenRepository.findByTokenId("token-1")).thenReturn(Optional.of(token1));
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.logoutAll("user-1");

        assertTrue(token1.isRevoked());
        assertEquals("LOGOUT_ALL", token1.getReplacedByTokenId());
    }

    @Test
    void sessionsMarkCurrentToken() {
        AuthEntity user = AuthEntity.builder()
                .uuid("user-1")
                .username("tester")
                .nickname("tester")
                .password("encoded")
                .build();

        RefreshTokenEntity[] savedToken = new RefreshTokenEntity[1];
        when(refreshTokenRepository.findAllByUserAndRevokedFalse(any(AuthEntity.class))).thenAnswer(invocation -> savedToken[0] == null ? List.of() : List.of(savedToken[0]));
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> {
            savedToken[0] = invocation.getArgument(0);
            return savedToken[0];
        });

        TokenResponseDto tokenResponse = authService.issueTokens(user);
        List<RefreshTokenSessionDto> sessions = authService.getSessions("user-1", tokenResponse.getRefreshToken());

        assertEquals(1, sessions.size());
        assertTrue(sessions.get(0).isCurrent());
    }
}
