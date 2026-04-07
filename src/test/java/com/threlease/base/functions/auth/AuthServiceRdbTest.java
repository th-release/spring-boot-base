package com.threlease.base.functions.auth;

import com.threlease.base.common.properties.app.jwt.JwtProperties;
import com.threlease.base.common.properties.app.redis.RedisProperties;
import com.threlease.base.common.properties.app.token.TokenProperties;
import com.threlease.base.common.provider.JwtProvider;
import com.threlease.base.common.utils.crypto.HashComponent;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.RefreshTokenEntity;
import com.threlease.base.functions.auth.dto.RefreshTokenSessionDto;
import com.threlease.base.functions.auth.dto.TokenResponseDto;
import com.threlease.base.repositories.auth.AuthRepository;
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
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final HashComponent hashComponent = new HashComponent();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQQ==");
        jwtProperties.validateSecretKey();

        JwtProvider jwtProvider = new JwtProvider(authRepository, jwtProperties);

        RedisProperties redisProperties = new RedisProperties();
        redisProperties.setEnabled(false);

        TokenProperties tokenProperties = new TokenProperties();
        tokenProperties.setStorage("rdb");
        tokenProperties.setMaxSessionsPerUser(5);

        ObjectProvider<StringRedisTemplate> objectProvider = mock(ObjectProvider.class);
        when(objectProvider.getIfAvailable()).thenReturn(null);

        authService = new AuthService(
                authRepository,
                refreshTokenRepository,
                jwtProvider,
                objectProvider,
                hashComponent,
                redisProperties,
                tokenProperties
        );
    }

    @Test
    void issueTokensAndGetSessionsWorkInRdbMode() {
        AuthEntity user = AuthEntity.builder()
                .uuid("user-1")
                .username("tester")
                .nickname("tester")
                .password("encoded")
                .salt("salt")
                .build();

        when(refreshTokenRepository.findAllByUserUuidAndRevokedFalse("user-1")).thenReturn(List.of());
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TokenResponseDto tokenResponse = authService.issueTokens(user);

        assertNotNull(tokenResponse.getAccessToken());
        assertNotNull(tokenResponse.getRefreshToken());
    }

    @Test
    void logoutAllRevokesActiveSessionsInRdbMode() {
        RefreshTokenEntity token1 = RefreshTokenEntity.builder()
                .id(1L)
                .userUuid("user-1")
                .tokenId("token-1")
                .familyId("family-1")
                .tokenHash("hash-1")
                .expiryDate(java.time.LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findAllByUserUuidAndRevokedFalse("user-1")).thenReturn(List.of(token1));
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
                .salt("salt")
                .build();

        RefreshTokenEntity[] savedToken = new RefreshTokenEntity[1];
        when(refreshTokenRepository.findAllByUserUuidAndRevokedFalse("user-1")).thenAnswer(invocation -> savedToken[0] == null ? List.of() : List.of(savedToken[0]));
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
