package com.threlease.base.repositories.auth;

import com.threlease.base.common.enums.AuthVerificationType;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.AuthVerificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuthVerificationRepository extends JpaRepository<AuthVerificationEntity, Long> {
    @Query("""
            SELECT a
            FROM AuthVerificationEntity a
            WHERE a.user = :user
              AND a.type = :type
              AND a.verified = false
            ORDER BY a.createdAt DESC
            """)
    Page<AuthVerificationEntity> findLatestByUserAndTypeAndVerifiedFalse(@Param("user") AuthEntity user,
                                                                         @Param("type") AuthVerificationType type,
                                                                         Pageable pageable);

    @Query("""
            SELECT a
            FROM AuthVerificationEntity a
            WHERE a.user = :user
              AND a.type = :type
              AND a.verified = false
            """)
    List<AuthVerificationEntity> findAllByUserAndTypeAndVerifiedFalse(@Param("user") AuthEntity user, @Param("type") AuthVerificationType type);

    @Query("""
            SELECT a
            FROM AuthVerificationEntity a
            WHERE a.user = :user
              AND a.type = :type
            """)
    List<AuthVerificationEntity> findAllByUserAndType(@Param("user") AuthEntity user, @Param("type") AuthVerificationType type);

    @Modifying
    @Query("""
            DELETE FROM AuthVerificationEntity a
            WHERE a.expiresAt < :now
               OR a.verified = true
            """)
    int deleteExpiredOrVerified(@Param("now") LocalDateTime now);
}
