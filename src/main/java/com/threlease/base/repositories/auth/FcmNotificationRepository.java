package com.threlease.base.repositories.auth;

import com.threlease.base.entities.FcmNotificationEntity;
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
            WHERE n.userUuid = :userUuid
              AND n.deletedAt IS NULL
            ORDER BY n.createdAt DESC
            """)
    List<FcmNotificationEntity> findAllActiveByUserUuid(@Param("userUuid") String userUuid, Pageable pageable);

    @Query("""
            SELECT n
            FROM FcmNotificationEntity n
            WHERE n.id = :id
              AND n.userUuid = :userUuid
              AND n.deletedAt IS NULL
            """)
    Optional<FcmNotificationEntity> findActiveByIdAndUserUuid(@Param("id") Long id, @Param("userUuid") String userUuid);
}
