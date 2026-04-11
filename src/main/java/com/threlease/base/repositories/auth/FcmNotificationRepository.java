package com.threlease.base.repositories.auth;

import com.threlease.base.entities.FcmNotificationEntity;
import com.threlease.base.entities.AuthEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FcmNotificationRepository extends JpaRepository<FcmNotificationEntity, String> {
    @Query("""
            SELECT n
            FROM FcmNotificationEntity n
            WHERE n.user = :user
              AND n.deletedAt IS NULL
            ORDER BY n.createdAt DESC
            """)
    List<FcmNotificationEntity> findAllActiveByUser(@Param("user") AuthEntity user, Pageable pageable);

    @Query("""
            SELECT n
            FROM FcmNotificationEntity n
            WHERE n.uuid = :uuid
              AND n.user = :user
              AND n.deletedAt IS NULL
            """)
    Optional<FcmNotificationEntity> findActiveByUuidAndUser(@Param("uuid") String uuid, @Param("user") AuthEntity user);
}
