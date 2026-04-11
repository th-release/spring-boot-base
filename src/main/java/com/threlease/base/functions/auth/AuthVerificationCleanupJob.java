package com.threlease.base.functions.auth;

import com.threlease.base.common.properties.app.auth.AuthSecurityProperties;
import com.threlease.base.repositories.auth.AuthVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthVerificationCleanupJob {
    private final AuthVerificationRepository authVerificationRepository;
    private final AuthSecurityProperties authSecurityProperties;

    @Scheduled(cron = "${app.auth.password-reset.cleanup-cron:0 30 3 * * *}")
    @Transactional
    public void cleanupExpiredVerifications() {
        if (!authSecurityProperties.getPasswordReset().isCleanupEnabled()) {
            return;
        }
        int deletedCount = authVerificationRepository.deleteExpiredOrVerified(LocalDateTime.now());
        if (deletedCount > 0) {
            log.info("[AuthVerificationCleanup] 만료/사용 완료 인증 데이터 {}건 삭제", deletedCount);
        }
    }
}
