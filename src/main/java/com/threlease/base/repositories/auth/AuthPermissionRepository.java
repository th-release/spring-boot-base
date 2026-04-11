package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuthPermissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuthPermissionRepository extends JpaRepository<AuthPermissionEntity, String> {
    @Query("""
            SELECT p
            FROM AuthPermissionEntity p
            WHERE p.code = :code
            ORDER BY p.createdAt DESC, p.uuid DESC
            """)
    Page<AuthPermissionEntity> findActiveByCode(@Param("code") String code, Pageable pageable);

    @Query("""
            SELECT p
            FROM AuthPermissionEntity p
            ORDER BY p.depth ASC, p.sortOrder ASC, p.uuid ASC
            """)
    List<AuthPermissionEntity> findAllActive();

    @Query("""
            SELECT p
            FROM AuthPermissionEntity p
            WHERE p.parent = :parent
            ORDER BY p.sortOrder ASC, p.uuid ASC
            """)
    List<AuthPermissionEntity> findAllActiveByParent(@Param("parent") AuthPermissionEntity parent);
}
