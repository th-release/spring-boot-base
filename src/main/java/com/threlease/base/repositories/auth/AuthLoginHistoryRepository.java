package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuthLoginHistoryEntity;
import com.threlease.base.entities.AuthEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthLoginHistoryRepository extends JpaRepository<AuthLoginHistoryEntity, Long> {
    @Query("""
            SELECT h
            FROM AuthLoginHistoryEntity h
            WHERE h.user = :user
              AND h.deletedAt IS NULL
            ORDER BY h.createdAt DESC, h.id DESC
            """)
    Page<AuthLoginHistoryEntity> findRecentByUser(@Param("user") AuthEntity user, Pageable pageable);

    @Query("""
            SELECT h
            FROM AuthLoginHistoryEntity h
            WHERE h.user = :user
              AND h.success = true
              AND h.deletedAt IS NULL
            ORDER BY h.createdAt DESC, h.id DESC
            """)
    Page<AuthLoginHistoryEntity> findRecentSuccessfulByUser(@Param("user") AuthEntity user, Pageable pageable);

    default Optional<AuthLoginHistoryEntity> findLatestByUserUuid(String userUuid) {
        return findRecentByUser(AuthEntity.builder().uuid(userUuid).build(), org.springframework.data.domain.PageRequest.of(0, 1)).stream().findFirst();
    }

    default Optional<AuthLoginHistoryEntity> findLatestSuccessfulByUserUuid(String userUuid) {
        return findRecentSuccessfulByUser(AuthEntity.builder().uuid(userUuid).build(), org.springframework.data.domain.PageRequest.of(0, 1)).stream().findFirst();
    }
}
