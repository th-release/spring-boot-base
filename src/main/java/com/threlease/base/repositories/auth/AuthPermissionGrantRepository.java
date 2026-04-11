package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuthPermissionGrantEntity;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.AuthPermissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuthPermissionGrantRepository extends JpaRepository<AuthPermissionGrantEntity, String> {
    @Query("""
            SELECT g
            FROM AuthPermissionGrantEntity g
            JOIN FETCH g.permission p
            WHERE g.user = :user
            ORDER BY p.depth ASC, p.sortOrder ASC, p.uuid ASC
            """)
    List<AuthPermissionGrantEntity> findAllActiveByUser(@Param("user") AuthEntity user);

    @Query("""
            SELECT g
            FROM AuthPermissionGrantEntity g
            WHERE g.user = :user
              AND g.permission = :permission
            ORDER BY g.createdAt DESC, g.uuid DESC
            """)
    Page<AuthPermissionGrantEntity> findLatestActiveByUserAndPermission(@Param("user") AuthEntity user,
                                                                        @Param("permission") AuthPermissionEntity permission,
                                                                        Pageable pageable);

    @Query("""
            SELECT g
            FROM AuthPermissionGrantEntity g
            JOIN FETCH g.user u
            WHERE g.permission = :permission
              AND u.deletedAt IS NULL
            ORDER BY g.createdAt ASC, g.uuid ASC
            """)
    List<AuthPermissionGrantEntity> findAllActiveByPermission(@Param("permission") AuthPermissionEntity permission);
}
