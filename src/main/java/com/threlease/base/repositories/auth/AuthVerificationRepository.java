package com.threlease.base.repositories.auth;

import com.threlease.base.common.enums.AuthVerificationType;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.AuthVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuthVerificationRepository extends JpaRepository<AuthVerificationEntity, Long> {
    @Query("""
            SELECT a
            FROM AuthVerificationEntity a
            WHERE a.user = :user
              AND a.type = :type
              AND a.verified = false
              AND a.deletedAt IS NULL
            ORDER BY a.createdAt DESC
            """)
    Optional<AuthVerificationEntity> findTopByUserAndTypeAndVerifiedFalseOrderByCreatedAtDesc(@Param("user") AuthEntity user, @Param("type") AuthVerificationType type);

    default Optional<AuthVerificationEntity> findTopByUserUuidAndTypeAndVerifiedFalseOrderByCreatedAtDesc(String userUuid, AuthVerificationType type) {
        return findTopByUserAndTypeAndVerifiedFalseOrderByCreatedAtDesc(AuthEntity.builder().uuid(userUuid).build(), type);
    }

    @Query("""
            SELECT a
            FROM AuthVerificationEntity a
            WHERE a.user = :user
              AND a.type = :type
              AND a.verified = false
              AND a.deletedAt IS NULL
            """)
    List<AuthVerificationEntity> findAllByUserAndTypeAndVerifiedFalse(@Param("user") AuthEntity user, @Param("type") AuthVerificationType type);

    @Query("""
            SELECT a
            FROM AuthVerificationEntity a
            WHERE a.user = :user
              AND a.type = :type
              AND a.deletedAt IS NULL
            """)
    List<AuthVerificationEntity> findAllByUserAndType(@Param("user") AuthEntity user, @Param("type") AuthVerificationType type);

    default List<AuthVerificationEntity> findAllByUserUuidAndType(String userUuid, AuthVerificationType type) {
        return findAllByUserAndType(AuthEntity.builder().uuid(userUuid).build(), type);
    }
}
