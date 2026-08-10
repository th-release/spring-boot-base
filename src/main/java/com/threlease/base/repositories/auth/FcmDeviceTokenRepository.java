package com.threlease.base.repositories.auth;

import com.threlease.base.entities.FcmDeviceTokenEntity;
import com.threlease.base.entities.AuthEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface FcmDeviceTokenRepository extends JpaRepository<FcmDeviceTokenEntity, String> {
    @Query("""
            SELECT f
            FROM FcmDeviceTokenEntity f
            WHERE f.deviceToken = :deviceToken
              AND f.deletedAt IS NULL
            ORDER BY f.lastUsedAt DESC
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Page<FcmDeviceTokenEntity> findLatestActiveByDeviceToken(@Param("deviceToken") String deviceToken, Pageable pageable);

    @Query("""
            SELECT f
            FROM FcmDeviceTokenEntity f
            WHERE f.deviceToken = :deviceToken
              AND f.deletedAt IS NULL
            ORDER BY f.lastUsedAt DESC
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<FcmDeviceTokenEntity> findAllActiveByDeviceToken(@Param("deviceToken") String deviceToken);

    @Query("""
            SELECT f
            FROM FcmDeviceTokenEntity f
            WHERE f.user = :user
              AND f.enabled = true
              AND f.deletedAt IS NULL
            ORDER BY f.lastUsedAt DESC
            """)
    List<FcmDeviceTokenEntity> findAllByUserAndEnabledTrueOrderByLastUsedAtDesc(@Param("user") AuthEntity user);

    @Query("""
            SELECT f
            FROM FcmDeviceTokenEntity f
            WHERE f.uuid = :uuid
              AND f.user = :user
              AND f.deletedAt IS NULL
            """)
    Optional<FcmDeviceTokenEntity> findByUuidAndUser(@Param("uuid") String uuid, @Param("user") AuthEntity user);

    @Modifying
    @Query("""
            UPDATE FcmDeviceTokenEntity f
            SET f.enabled = false,
                f.deletedAt = CURRENT_TIMESTAMP
            WHERE f.deviceToken = :deviceToken
              AND f.deletedAt IS NULL
            """)
    int disableAllActiveByDeviceToken(@Param("deviceToken") String deviceToken);
}
