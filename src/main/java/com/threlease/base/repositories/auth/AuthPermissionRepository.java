package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuthPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuthPermissionRepository extends JpaRepository<AuthPermissionEntity, Long> {
    @Query("""
            SELECT p
            FROM AuthPermissionEntity p
            WHERE p.code = :code
              AND p.deletedAt IS NULL
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<AuthPermissionEntity> findAllActiveByCode(@Param("code") String code);

    default Optional<AuthPermissionEntity> findActiveByCode(String code) {
        return findAllActiveByCode(code).stream().findFirst();
    }

    @Query("""
            SELECT p
            FROM AuthPermissionEntity p
            WHERE p.deletedAt IS NULL
            ORDER BY p.depth ASC, p.sortOrder ASC, p.id ASC
            """)
    List<AuthPermissionEntity> findAllActive();

    @Query("""
            SELECT p
            FROM AuthPermissionEntity p
            WHERE p.parentId = :parentId
              AND p.deletedAt IS NULL
            ORDER BY p.sortOrder ASC, p.id ASC
            """)
    List<AuthPermissionEntity> findAllActiveByParentId(@Param("parentId") Long parentId);
}
