package com.threlease.base.functions.auth;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.utils.DeviceUtils;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.FcmDeviceTokenEntity;
import com.threlease.base.repositories.auth.FcmDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmDeviceTokenService {
    private final FcmDeviceTokenRepository fcmDeviceTokenRepository;

    public List<FcmDeviceTokenEntity> getMyTokens(String userUuid) {
        return fcmDeviceTokenRepository.findAllByUserAndEnabledTrueOrderByLastUsedAtDesc(AuthEntity.builder().uuid(userUuid).build());
    }

    @Transactional
    public FcmDeviceTokenEntity register(AuthEntity auth, String deviceToken, String deviceLabel, String userAgent, String ipAddress) {
        List<FcmDeviceTokenEntity> existingTokens = fcmDeviceTokenRepository.findAllActiveByDeviceToken(deviceToken);
        String normalizedDeviceLabel = deviceLabel == null || deviceLabel.isBlank() ? DeviceUtils.describe(userAgent) : deviceLabel;
        String normalizedUserAgent = userAgent == null ? null : userAgent.substring(0, Math.min(userAgent.length(), 512));
        String normalizedIpAddress = ipAddress == null ? null : ipAddress.substring(0, Math.min(ipAddress.length(), 64));
        LocalDateTime now = LocalDateTime.now();

        FcmDeviceTokenEntity ownedToken = existingTokens.stream()
                .filter(existing -> existing.getUser() != null && auth.getUuid() != null && auth.getUuid().equals(existing.getUser().getUuid()))
                .findFirst()
                .orElse(null);

        for (FcmDeviceTokenEntity existing : existingTokens) {
            if (existing == ownedToken) {
                continue;
            }
            existing.delete();
            existing.setEnabled(false);
            fcmDeviceTokenRepository.save(existing);
        }

        if (ownedToken != null) {
            ownedToken.setDeviceLabel(normalizedDeviceLabel);
            ownedToken.setUserAgent(normalizedUserAgent);
            ownedToken.setLastIpAddress(normalizedIpAddress);
            ownedToken.setLastUsedAt(now);
            ownedToken.setEnabled(true);
            return fcmDeviceTokenRepository.save(ownedToken);
        }

        FcmDeviceTokenEntity entity = FcmDeviceTokenEntity.builder().deviceToken(deviceToken).build();
        entity.setUser(auth);
        entity.setDeviceLabel(normalizedDeviceLabel);
        entity.setUserAgent(normalizedUserAgent);
        entity.setLastIpAddress(normalizedIpAddress);
        entity.setLastUsedAt(now);
        entity.setEnabled(true);
        return fcmDeviceTokenRepository.save(entity);
    }

    public void disableMyToken(String userUuid, String tokenUuid) {
        FcmDeviceTokenEntity entity = fcmDeviceTokenRepository.findByUuidAndUser(tokenUuid, AuthEntity.builder().uuid(userUuid).build())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        entity.setEnabled(false);
        fcmDeviceTokenRepository.save(entity);
    }

    @Transactional
    public int disableByDeviceToken(String deviceToken) {
        int disabledCount = 0;
        for (FcmDeviceTokenEntity entity : fcmDeviceTokenRepository.findAllActiveByDeviceToken(deviceToken)) {
            entity.delete();
            entity.setEnabled(false);
            fcmDeviceTokenRepository.save(entity);
            disabledCount++;
        }
        return disabledCount;
    }

    public List<FcmDeviceTokenEntity> getTokensForUser(String userUuid) {
        return fcmDeviceTokenRepository.findAllByUserAndEnabledTrueOrderByLastUsedAtDesc(AuthEntity.builder().uuid(userUuid).build());
    }
}
