package com.threlease.base.functions.auth;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.utils.firebase.FirebaseUtils;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.FcmDeviceTokenEntity;
import com.threlease.base.functions.auth.dto.FcmDeviceTokenDto;
import com.threlease.base.functions.auth.dto.FcmDeviceTokenRequestDto;
import com.threlease.base.functions.auth.dto.FcmNotificationDto;
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
    private final FcmNotificationService fcmNotificationService;

    public List<FcmDeviceTokenDto> getMyTokens(AuthEntity user) {
        return fcmDeviceTokenService.getMyTokens(user.getUuid()).stream()
                .map(this::toDto)
                .toList();
    }

    public FcmDeviceTokenDto registerToken(AuthEntity user, FcmDeviceTokenRequestDto dto, String userAgent, String ipAddress) {
        FcmDeviceTokenEntity token = fcmDeviceTokenService.register(user, dto.getDeviceToken(), dto.getDeviceLabel(), userAgent, ipAddress);
        return toDto(token);
    }

    public void disableMyToken(AuthEntity user, String tokenUuid) {
        fcmDeviceTokenService.disableMyToken(user.getUuid(), tokenUuid);
    }

    public List<FcmNotificationDto> getMyNotifications(AuthEntity user, int page, int size) {
        return fcmNotificationService.getMyNotifications(user.getUuid(), page, size);
    }

    public FcmNotificationDto markMyNotificationRead(AuthEntity user, String notificationUuid) {
        return fcmNotificationService.markMyNotificationRead(user.getUuid(), notificationUuid);
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
                        String messageId = firebaseUtils.sendNotification(token.getDeviceToken(), dto.getTitle(), dto.getBody(), dto.getData());
                        fcmNotificationService.saveSentNotification(uuid, messageId, dto.getTitle(), dto.getBody(), dto.getData());
                        return messageId;
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
                .uuid(token.getUuid())
                .deviceLabel(token.getDeviceLabel())
                .userAgent(token.getUserAgent())
                .lastIpAddress(token.getLastIpAddress())
                .lastUsedAt(token.getLastUsedAt())
                .enabled(token.isEnabled())
                .build();
    }
}
