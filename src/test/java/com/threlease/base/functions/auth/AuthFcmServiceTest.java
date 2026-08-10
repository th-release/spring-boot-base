package com.threlease.base.functions.auth;

import com.threlease.base.common.utils.firebase.FirebaseUtils;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.FcmDeviceTokenEntity;
import com.threlease.base.functions.auth.dto.FcmPushResultDto;
import com.threlease.base.functions.auth.dto.FcmPushRequestDto;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthFcmServiceTest {

    @Test
    void pushToUserContinuesWhenOneDeviceFails() throws Exception {
        FcmDeviceTokenService tokenService = mock(FcmDeviceTokenService.class);
        FirebaseUtils firebaseUtils = mock(FirebaseUtils.class);
        AuditLogService auditLogService = mock(AuditLogService.class);
        AuthAdminService authAdminService = mock(AuthAdminService.class);
        FcmNotificationService notificationService = mock(FcmNotificationService.class);

        AuthFcmService service = new AuthFcmService(tokenService, firebaseUtils, auditLogService, authAdminService, notificationService);
        AuthEntity admin = AuthEntity.builder().uuid("admin-1").build();

        FcmDeviceTokenEntity token1 = FcmDeviceTokenEntity.builder().uuid("fcm-1").deviceToken("token-1").build();
        FcmDeviceTokenEntity token2 = FcmDeviceTokenEntity.builder().uuid("fcm-2").deviceToken("token-2").build();
        when(tokenService.getTokensForUser("user-1")).thenReturn(List.of(token1, token2));
        when(firebaseUtils.isEnabled()).thenReturn(true);
        when(firebaseUtils.sendNotification("token-1", "title", "body", null)).thenReturn("msg-1");
        when(firebaseUtils.sendNotification("token-2", "title", "body", null)).thenThrow(new RuntimeException("boom"));

        FcmPushRequestDto dto = new FcmPushRequestDto();
        dto.setTitle("title");
        dto.setBody("body");

        FcmPushResultDto result = service.pushToUser(admin, "user-1", dto, null);

        assertEquals(List.of("msg-1"), result.getMessageIds());
        assertEquals(1, result.getFailedTokenUuids().size());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    @Test
    void invalidDeviceTokenIsDisabledWhenFirebaseRejectsIt() throws Exception {
        FcmDeviceTokenService tokenService = mock(FcmDeviceTokenService.class);
        FirebaseUtils firebaseUtils = mock(FirebaseUtils.class);
        AuditLogService auditLogService = mock(AuditLogService.class);
        AuthAdminService authAdminService = mock(AuthAdminService.class);
        FcmNotificationService notificationService = mock(FcmNotificationService.class);

        AuthFcmService service = new AuthFcmService(tokenService, firebaseUtils, auditLogService, authAdminService, notificationService);
        AuthEntity admin = AuthEntity.builder().uuid("admin-1").build();

        FcmDeviceTokenEntity token = FcmDeviceTokenEntity.builder().uuid("fcm-1").deviceToken("token-1").build();
        when(tokenService.getTokensForUser("user-1")).thenReturn(List.of(token));
        when(firebaseUtils.isEnabled()).thenReturn(true);

        FirebaseMessagingException firebaseException = mock(FirebaseMessagingException.class);
        when(firebaseException.getMessagingErrorCode()).thenReturn(MessagingErrorCode.UNREGISTERED);
        when(firebaseUtils.sendNotification("token-1", "title", "body", null)).thenThrow(firebaseException);

        FcmPushRequestDto dto = new FcmPushRequestDto();
        dto.setTitle("title");
        dto.setBody("body");

        FcmPushResultDto result = service.pushToUser(admin, "user-1", dto, null);

        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        verify(tokenService).disableByDeviceToken("token-1");
    }
}
