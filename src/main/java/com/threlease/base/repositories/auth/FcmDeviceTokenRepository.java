package com.threlease.base.repositories.auth;

import com.threlease.base.entities.FcmDeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FcmDeviceTokenRepository extends JpaRepository<FcmDeviceTokenEntity, Long> {
    @Query("SELECT f FROM FcmDeviceTokenEntity f WHERE f.deviceToken = :deviceToken")
    Optional<FcmDeviceTokenEntity> findByDeviceToken(String deviceToken);

    @Query("""
            SELECT f
            FROM FcmDeviceTokenEntity f
            WHERE f.userUuid = :userUuid
              AND f.enabled = true
            ORDER BY f.lastUsedAt DESC
            """)
    List<FcmDeviceTokenEntity> findAllByUserUuidAndEnabledTrueOrderByLastUsedAtDesc(String userUuid);

    @Query("SELECT f FROM FcmDeviceTokenEntity f WHERE f.id = :id AND f.userUuid = :userUuid")
    Optional<FcmDeviceTokenEntity> findByIdAndUserUuid(Long id, String userUuid);
}
