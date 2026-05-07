package com.threlease.base.functions.auth;

import com.threlease.base.common.properties.app.token.TokenProperties;
import com.threlease.base.entities.RefreshTokenEntity;
import com.threlease.base.repositories.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProperties tokenProperties;

    @Scheduled(cron = "${app.token.cleanup.cron:0 45 3 * * *}")
    @Transactional
    public void cleanupRevokedRefreshTokens() {
        if (!"rdb".equalsIgnoreCase(tokenProperties.getStorage())) {
            return;
        }
        if (!tokenProperties.getCleanup().isEnabled()) {
            return;
        }

        List<RefreshTokenEntity> revokedTokens = refreshTokenRepository.findAllRevoked();
        if (revokedTokens.isEmpty()) {
            return;
        }
        refreshTokenRepository.deleteAll(revokedTokens);

        log.info("[RefreshTokenCleanup] revoked refresh token {}건 삭제", revokedTokens.size());
    }
}
