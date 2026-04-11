package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuthLoginHistoryEntity;
import com.threlease.base.entities.AuthEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthLoginHistoryRepository extends JpaRepository<AuthLoginHistoryEntity, String> {
    @Query("""
            SELECT h
            FROM AuthLoginHistoryEntity h
            WHERE h.user = :user
            ORDER BY h.createdAt DESC, h.id DESC
            """)
    Page<AuthLoginHistoryEntity> findRecentByUser(@Param("user") AuthEntity user, Pageable pageable);

    @Query("""
            SELECT h
            FROM AuthLoginHistoryEntity h
            WHERE h.user = :user
              AND h.success = true
            ORDER BY h.createdAt DESC, h.id DESC
            """)
    Page<AuthLoginHistoryEntity> findRecentSuccessfulByUser(@Param("user") AuthEntity user, Pageable pageable);
}
