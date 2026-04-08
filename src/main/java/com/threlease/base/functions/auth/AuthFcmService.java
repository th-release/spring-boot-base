package com.threlease.base.functions.auth;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.utils.firebase.FirebaseUtils;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.FcmDeviceTokenEntity;
import com.threlease.base.functions.auth.dto.FcmDeviceTokenDto;
import com.threlease.base.functions.auth.dto.FcmDeviceTokenRequestDto;
import com.threlease.base.functions.auth.dto.FcmPushRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthFcmService {
    private final FcmDeviceTokenService fcmDeviceTokenService;
    private final FirebaseUtils firebaseUtils;
    private final AuditLogService auditLogService;
    private final AuthAdminService authAdminService;

    public List<FcmDeviceTokenDto> getMyTokens(AuthEntity user) {
        return fcmDeviceTokenService.getMyTokens(user.getUuid()).stream()
                .map(this::toDto)
                .toList();
    }

    public FcmDeviceTokenDto registerToken(AuthEntity user, FcmDeviceTokenRequestDto dto, String userAgent, String ipAddress) {
        FcmDeviceTokenEntity token = fcmDeviceTokenService.register(user, dto.getDeviceToken(), dto.getDeviceLabel(), userAgent, ipAddress);
        return toDto(token);
    }

    public void disableMyToken(AuthEntity user, Long id) {
        fcmDeviceTokenService.disableMyToken(user.getUuid(), id);
    }

    public List<FcmDeviceTokenDto> getUserTokens(AuthEntity admin, String uuid) {
        authAdminService.assertAdmin(admin);
        return fcmDeviceTokenService.getTokensForUser(uuid).stream()
                .map(this::toDto)
                .toList();
    }

    public List<String> pushToUser(AuthEntity admin, String uuid, FcmPushRequestDto dto, HttpServletRequest request) throws Exception {
        authAdminService.assertAdmin(admin);
        if (!firebaseUtils.isEnabled()) {
            throw new BusinessException(ErrorCode.FIREBASE_DISABLED);
        }

        List<String> messageIds = fcmDeviceTokenService.getTokensForUser(uuid).stream()
                .map(token -> {
                    try {
                        return firebaseUtils.sendNotification(token.getDeviceToken(), dto.getTitle(), dto.getBody(), dto.getData());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
        auditLogService.logAdmin(admin.getUuid(), "ADMIN_SEND_FCM_PUSH", "FCM", uuid, true, request, "Admin sent FCM push to user devices");
        return messageIds;
    }

    private FcmDeviceTokenDto toDto(FcmDeviceTokenEntity token) {
        return FcmDeviceTokenDto.builder()
                .id(token.getId())
                .deviceLabel(token.getDeviceLabel())
                .userAgent(token.getUserAgent())
                .lastIpAddress(token.getLastIpAddress())
                .lastUsedAt(token.getLastUsedAt())
                .enabled(token.isEnabled())
                .build();
    }
}
