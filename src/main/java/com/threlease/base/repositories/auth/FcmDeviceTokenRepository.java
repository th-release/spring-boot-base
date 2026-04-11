package com.threlease.base.repositories.auth;

import com.threlease.base.entities.FcmDeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FcmDeviceTokenRepository extends JpaRepository<FcmDeviceTokenEntity, Long> {
    @Query("""
            SELECT f
            FROM FcmDeviceTokenEntity f
            WHERE f.deviceToken = :deviceToken
              AND f.deletedAt IS NULL
            ORDER BY f.lastUsedAt DESC
            """)
    List<FcmDeviceTokenEntity> findAllActiveByDeviceToken(@Param("deviceToken") String deviceToken);

    default Optional<FcmDeviceTokenEntity> findByDeviceToken(String deviceToken) {
        return findAllActiveByDeviceToken(deviceToken).stream().findFirst();
    }

    @Query("""
            SELECT f
            FROM FcmDeviceTokenEntity f
            WHERE f.userUuid = :userUuid
              AND f.enabled = true
              AND f.deletedAt IS NULL
            ORDER BY f.lastUsedAt DESC
            """)
    List<FcmDeviceTokenEntity> findAllByUserUuidAndEnabledTrueOrderByLastUsedAtDesc(@Param("userUuid") String userUuid);

    @Query("""
            SELECT f
            FROM FcmDeviceTokenEntity f
            WHERE f.id = :id
              AND f.userUuid = :userUuid
              AND f.deletedAt IS NULL
            """)
    Optional<FcmDeviceTokenEntity> findByIdAndUserUuid(@Param("id") Long id, @Param("userUuid") String userUuid);
}
