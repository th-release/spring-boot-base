package com.threlease.base.functions.auth;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.utils.DeviceUtils;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.FcmDeviceTokenEntity;
import com.threlease.base.repositories.auth.FcmDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmDeviceTokenService {
    private final FcmDeviceTokenRepository fcmDeviceTokenRepository;

    public List<FcmDeviceTokenEntity> getMyTokens(String userUuid) {
        return fcmDeviceTokenRepository.findAllByUserUuidAndEnabledTrueOrderByLastUsedAtDesc(userUuid);
    }

    public FcmDeviceTokenEntity register(AuthEntity auth, String deviceToken, String deviceLabel, String userAgent, String ipAddress) {
        FcmDeviceTokenEntity entity = fcmDeviceTokenRepository.findByDeviceToken(deviceToken)
                .orElse(FcmDeviceTokenEntity.builder().deviceToken(deviceToken).build());
        entity.setUser(auth);
        entity.setDeviceLabel(deviceLabel == null || deviceLabel.isBlank() ? DeviceUtils.describe(userAgent) : deviceLabel);
        entity.setUserAgent(userAgent == null ? null : userAgent.substring(0, Math.min(userAgent.length(), 512)));
        entity.setLastIpAddress(ipAddress == null ? null : ipAddress.substring(0, Math.min(ipAddress.length(), 64)));
        entity.setLastUsedAt(LocalDateTime.now());
        entity.setEnabled(true);
        return fcmDeviceTokenRepository.save(entity);
    }

    public void disableMyToken(String userUuid, Long id) {
        FcmDeviceTokenEntity entity = fcmDeviceTokenRepository.findByIdAndUserUuid(id, userUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        entity.setEnabled(false);
        fcmDeviceTokenRepository.save(entity);
    }

    public List<FcmDeviceTokenEntity> getTokensForUser(String userUuid) {
        return fcmDeviceTokenRepository.findAllByUserUuidAndEnabledTrueOrderByLastUsedAtDesc(userUuid);
    }
}
