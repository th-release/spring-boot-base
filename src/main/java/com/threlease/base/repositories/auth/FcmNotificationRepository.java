package com.threlease.base.repositories.auth;

import com.threlease.base.entities.FcmNotificationEntity;
import com.threlease.base.entities.AuthEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FcmNotificationRepository extends JpaRepository<FcmNotificationEntity, Long> {
    @Query("""
            SELECT n
            FROM FcmNotificationEntity n
            WHERE n.user = :user
              AND n.deletedAt IS NULL
            ORDER BY n.createdAt DESC
            """)
    List<FcmNotificationEntity> findAllActiveByUser(@Param("user") AuthEntity user, Pageable pageable);

    default List<FcmNotificationEntity> findAllActiveByUserUuid(String userUuid, Pageable pageable) {
        return findAllActiveByUser(AuthEntity.builder().uuid(userUuid).build(), pageable);
    }

    @Query("""
            SELECT n
            FROM FcmNotificationEntity n
            WHERE n.id = :id
              AND n.user = :user
              AND n.deletedAt IS NULL
            """)
    Optional<FcmNotificationEntity> findActiveByIdAndUser(@Param("id") Long id, @Param("user") AuthEntity user);

    default Optional<FcmNotificationEntity> findActiveByIdAndUserUuid(Long id, String userUuid) {
        return findActiveByIdAndUser(id, AuthEntity.builder().uuid(userUuid).build());
    }
}
