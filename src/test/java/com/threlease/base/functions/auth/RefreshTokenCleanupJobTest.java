package com.threlease.base.functions.auth;

import com.threlease.base.common.properties.app.token.TokenProperties;
import com.threlease.base.entities.RefreshTokenEntity;
import com.threlease.base.repositories.auth.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenCleanupJobTest {
    private RefreshTokenRepository refreshTokenRepository;
    private TokenProperties tokenProperties;
    private RefreshTokenCleanupJob refreshTokenCleanupJob;

    @BeforeEach
    void setUp() {
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        tokenProperties = new TokenProperties();
        tokenProperties.setStorage("rdb");
        tokenProperties.getCleanup().setEnabled(true);
        refreshTokenCleanupJob = new RefreshTokenCleanupJob(refreshTokenRepository, tokenProperties);
    }

    @Test
    void cleanupDeletesRevokedRefreshTokensInRdbMode() {
        RefreshTokenEntity token1 = RefreshTokenEntity.builder().uuid("token-1").tokenId("token-id-1").build();
        RefreshTokenEntity token2 = RefreshTokenEntity.builder().uuid("token-2").tokenId("token-id-2").build();

        when(refreshTokenRepository.findAllRevoked()).thenReturn(List.of(token1, token2));

        refreshTokenCleanupJob.cleanupRevokedRefreshTokens();

        verify(refreshTokenRepository).findAllRevoked();
        verify(refreshTokenRepository).deleteAll(List.of(token1, token2));
    }

    @Test
    void cleanupSkipsWhenStorageIsNotRdb() {
        tokenProperties.setStorage("cache");

        refreshTokenCleanupJob.cleanupRevokedRefreshTokens();

        verify(refreshTokenRepository, never()).findAllRevoked();
        verify(refreshTokenRepository, never()).deleteAll(any());
    }
}
