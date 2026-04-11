package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuthMfaEntity;
import com.threlease.base.entities.AuthEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthMfaRepository extends JpaRepository<AuthMfaEntity, Long> {
    @Query("""
            SELECT m
            FROM AuthMfaEntity m
            WHERE m.user = :user
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    Page<AuthMfaEntity> findLatestActiveByUser(@Param("user") AuthEntity user, Pageable pageable);
}
