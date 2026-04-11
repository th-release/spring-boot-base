package com.threlease.base.functions.auth;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.utils.DeviceUtils;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.FcmDeviceTokenEntity;
import com.threlease.base.repositories.auth.FcmDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmDeviceTokenService {
    private final FcmDeviceTokenRepository fcmDeviceTokenRepository;

    public List<FcmDeviceTokenEntity> getMyTokens(String userUuid) {
        return fcmDeviceTokenRepository.findAllByUserAndEnabledTrueOrderByLastUsedAtDesc(AuthEntity.builder().uuid(userUuid).build());
    }

    public FcmDeviceTokenEntity register(AuthEntity auth, String deviceToken, String deviceLabel, String userAgent, String ipAddress) {
        FcmDeviceTokenEntity entity = fcmDeviceTokenRepository.findLatestActiveByDeviceToken(deviceToken, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(FcmDeviceTokenEntity.builder().deviceToken(deviceToken).build());
        entity.setUser(auth);
        entity.setDeviceLabel(deviceLabel == null || deviceLabel.isBlank() ? DeviceUtils.describe(userAgent) : deviceLabel);
        entity.setUserAgent(userAgent == null ? null : userAgent.substring(0, Math.min(userAgent.length(), 512)));
        entity.setLastIpAddress(ipAddress == null ? null : ipAddress.substring(0, Math.min(ipAddress.length(), 64)));
        entity.setLastUsedAt(LocalDateTime.now());
        entity.setEnabled(true);
        return fcmDeviceTokenRepository.save(entity);
    }

    public void disableMyToken(String userUuid, String tokenUuid) {
        FcmDeviceTokenEntity entity = fcmDeviceTokenRepository.findByUuidAndUser(tokenUuid, AuthEntity.builder().uuid(userUuid).build())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        entity.setEnabled(false);
        fcmDeviceTokenRepository.save(entity);
    }

    public List<FcmDeviceTokenEntity> getTokensForUser(String userUuid) {
        return fcmDeviceTokenRepository.findAllByUserAndEnabledTrueOrderByLastUsedAtDesc(AuthEntity.builder().uuid(userUuid).build());
    }
}
