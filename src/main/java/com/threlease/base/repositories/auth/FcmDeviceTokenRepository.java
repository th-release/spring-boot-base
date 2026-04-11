package com.threlease.base.repositories.auth;

import com.threlease.base.entities.FcmDeviceTokenEntity;
import com.threlease.base.entities.AuthEntity;
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
            WHERE f.user = :user
              AND f.enabled = true
              AND f.deletedAt IS NULL
            ORDER BY f.lastUsedAt DESC
            """)
    List<FcmDeviceTokenEntity> findAllByUserAndEnabledTrueOrderByLastUsedAtDesc(@Param("user") AuthEntity user);

    default List<FcmDeviceTokenEntity> findAllByUserUuidAndEnabledTrueOrderByLastUsedAtDesc(String userUuid) {
        return findAllByUserAndEnabledTrueOrderByLastUsedAtDesc(AuthEntity.builder().uuid(userUuid).build());
    }

    @Query("""
            SELECT f
            FROM FcmDeviceTokenEntity f
            WHERE f.id = :id
              AND f.user = :user
              AND f.deletedAt IS NULL
            """)
    Optional<FcmDeviceTokenEntity> findByIdAndUser(@Param("id") Long id, @Param("user") AuthEntity user);

    default Optional<FcmDeviceTokenEntity> findByIdAndUserUuid(Long id, String userUuid) {
        return findByIdAndUser(id, AuthEntity.builder().uuid(userUuid).build());
    }
}
