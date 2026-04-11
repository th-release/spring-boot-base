package com.threlease.base.repositories.auth;

import com.threlease.base.common.enums.AuthVerificationType;
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
            WHERE a.userUuid = :userUuid
              AND a.type = :type
              AND a.verified = false
              AND a.deletedAt IS NULL
            ORDER BY a.createdAt DESC
            """)
    Optional<AuthVerificationEntity> findTopByUserUuidAndTypeAndVerifiedFalseOrderByCreatedAtDesc(@Param("userUuid") String userUuid, @Param("type") AuthVerificationType type);

    @Query("""
            SELECT a
            FROM AuthVerificationEntity a
            WHERE a.userUuid = :userUuid
              AND a.type = :type
              AND a.verified = false
              AND a.deletedAt IS NULL
            """)
    List<AuthVerificationEntity> findAllByUserUuidAndTypeAndVerifiedFalse(@Param("userUuid") String userUuid, @Param("type") AuthVerificationType type);

    @Query("""
            SELECT a
            FROM AuthVerificationEntity a
            WHERE a.userUuid = :userUuid
              AND a.type = :type
              AND a.deletedAt IS NULL
            """)
    List<AuthVerificationEntity> findAllByUserUuidAndType(@Param("userUuid") String userUuid, @Param("type") AuthVerificationType type);
}
