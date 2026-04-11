package com.threlease.base.functions.auth;

import com.google.gson.Gson;
import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.FcmNotificationEntity;
import com.threlease.base.functions.auth.dto.FcmNotificationDto;
import com.threlease.base.repositories.auth.FcmNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FcmNotificationService {
    private final FcmNotificationRepository fcmNotificationRepository;
    private final Gson gson = new Gson();

    public FcmNotificationEntity saveSentNotification(String userUuid,
                                                      String messageId,
                                                      String title,
                                                      String body,
                                                      Map<String, String> data) {
        FcmNotificationEntity notification = FcmNotificationEntity.builder()
                .user(AuthEntity.builder().uuid(userUuid).build())
                .messageId(messageId)
                .title(trim(title, 255))
                .body(body)
                .data(data == null || data.isEmpty() ? null : gson.toJson(data))
                .read(false)
                .build();
        return fcmNotificationRepository.save(notification);
    }

    public List<FcmNotificationDto> getMyNotifications(String userUuid, int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return fcmNotificationRepository.findAllActiveByUser(AuthEntity.builder().uuid(userUuid).build(), pageRequest).stream()
                .map(this::toDto)
                .toList();
    }

    public FcmNotificationDto markMyNotificationRead(String userUuid, String notificationUuid) {
        FcmNotificationEntity notification = fcmNotificationRepository.findActiveByUuidAndUser(notificationUuid, AuthEntity.builder().uuid(userUuid).build())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        notification.markRead();
        return toDto(fcmNotificationRepository.save(notification));
    }

    private FcmNotificationDto toDto(FcmNotificationEntity notification) {
        return FcmNotificationDto.builder()
                .uuid(notification.getUuid())
                .messageId(notification.getMessageId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .data(notification.getData())
                .read(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.substring(0, Math.min(value.length(), maxLength));
    }
}
