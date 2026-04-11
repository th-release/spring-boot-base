package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.AuthLoginFailureEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthLoginFailureRepository extends JpaRepository<AuthLoginFailureEntity, Long> {
    @Query("""
            SELECT f
            FROM AuthLoginFailureEntity f
            WHERE f.user = :user
              AND f.deletedAt IS NULL
            ORDER BY f.createdAt DESC, f.id DESC
            """)
    Page<AuthLoginFailureEntity> findLatestByUser(@Param("user") AuthEntity user, Pageable pageable);
}
