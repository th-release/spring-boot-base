package com.threlease.base.functions.auth;

import com.threlease.base.common.properties.app.auth.AuthSecurityProperties;
import com.threlease.base.common.properties.app.privacy.PrivacyProperties;
import com.threlease.base.common.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogService {
    private final AuthSecurityProperties authSecurityProperties;
    private final PrivacyProperties privacyProperties;

    public void log(String actorUuid, String action, String resourceType, String resourceId, boolean success,
                    HttpServletRequest request, String detail) {
        log.info("AUTH EVENT actor={}, action={}, resourceType={}, resourceId={}, success={}, ip={}, detail={}",
                actorUuid,
                action,
                resourceType,
                resourceId,
                success,
                maskIpIfNeeded(request != null ? IpUtils.getClientIp(request) : null),
                detail);
    }

    public void logAdmin(String actorUuid, String action, String resourceType, String resourceId, boolean success,
                         HttpServletRequest request, String detail) {
        if (!authSecurityProperties.getAudit().isEnabled()) {
            return;
        }
        log.info("ADMIN EVENT actor={}, action={}, resourceType={}, resourceId={}, success={}, ip={}, userAgent={}, detail={}",
                actorUuid,
                action,
                resourceType,
                resourceId,
                success,
                maskIpIfNeeded(request != null ? IpUtils.getClientIp(request) : null),
                resolveUserAgent(request),
                trim(detail, 255));
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

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.substring(0, Math.min(value.length(), maxLength));
    }
}
