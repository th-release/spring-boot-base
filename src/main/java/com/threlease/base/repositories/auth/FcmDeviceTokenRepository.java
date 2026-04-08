package com.threlease.base.repositories.auth;

import com.threlease.base.entities.FcmDeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmDeviceTokenRepository extends JpaRepository<FcmDeviceTokenEntity, Long> {
    Optional<FcmDeviceTokenEntity> findByDeviceToken(String deviceToken);
    List<FcmDeviceTokenEntity> findAllByUserUuidAndEnabledTrueOrderByLastUsedAtDesc(String userUuid);
    Optional<FcmDeviceTokenEntity> findByIdAndUserUuid(Long id, String userUuid);
}
