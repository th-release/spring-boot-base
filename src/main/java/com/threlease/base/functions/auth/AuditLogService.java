package com.threlease.base.functions.auth;

import com.threlease.base.common.properties.app.auth.AuthSecurityProperties;
import com.threlease.base.common.properties.app.privacy.PrivacyProperties;
import com.threlease.base.common.utils.IpUtils;
import com.threlease.base.entities.AuditLogEntity;
import com.threlease.base.functions.auth.dto.AuditLogDto;
import com.threlease.base.repositories.auth.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AuthSecurityProperties authSecurityProperties;
    private final PrivacyProperties privacyProperties;

    public void log(String actorUuid, String action, String resourceType, String resourceId, boolean success,
                    HttpServletRequest request, String detail) {
        if (!authSecurityProperties.getAudit().isEnabled()) {
            return;
        }

        auditLogRepository.save(AuditLogEntity.builder()
                .actorUuid(actorUuid)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .success(success)
                .clientIp(maskIpIfNeeded(request != null ? IpUtils.getClientIp(request) : null))
                .userAgent(resolveUserAgent(request))
                .detail(detail)
                .build());
    }

    public Page<AuditLogDto> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(entity -> AuditLogDto.builder()
                        .id(entity.getId())
                        .actorUuid(entity.getActorUuid())
                        .action(entity.getAction())
                        .resourceType(entity.getResourceType())
                        .resourceId(entity.getResourceId())
                        .success(entity.isSuccess())
                        .clientIp(entity.getClientIp())
                        .userAgent(entity.getUserAgent())
                        .detail(entity.getDetail())
                        .createdAt(entity.getCreatedAt())
                        .build());
    }

    @Scheduled(cron = "0 30 4 * * *")
    public void cleanupExpiredAuditLogs() {
        auditLogRepository.deleteByCreatedAtBefore(LocalDateTime.now().minusDays(privacyProperties.getAuditRetentionDays()));
    }

    private String resolveUserAgent(HttpServletRequest request) {
        if (request == null || !authSecurityProperties.getAudit().isIncludeUserAgent() || !privacyProperties.isIncludeUserAgent()) {
            return null;
        }
        String userAgent = request.getHeader("User-Agent");
        return userAgent == null ? null : userAgent.substring(0, Math.min(userAgent.length(), 512));
    }

    private String maskIpIfNeeded(String ip) {
        if (ip == null || !privacyProperties.isMaskAuditIp()) {
            return ip;
        }
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".***";
        }
        return ip;
    }
}
