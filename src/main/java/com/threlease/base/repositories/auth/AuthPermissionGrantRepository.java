package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuthPermissionGrantEntity;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.AuthPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuthPermissionGrantRepository extends JpaRepository<AuthPermissionGrantEntity, Long> {
    @Query("""
            SELECT g
            FROM AuthPermissionGrantEntity g
            JOIN FETCH g.permission p
            WHERE g.user = :user
              AND g.deletedAt IS NULL
              AND p.deletedAt IS NULL
            ORDER BY p.depth ASC, p.sortOrder ASC, p.id ASC
            """)
    List<AuthPermissionGrantEntity> findAllActiveByUser(@Param("user") AuthEntity user);

    default List<AuthPermissionGrantEntity> findAllActiveByUserUuid(String userUuid) {
        return findAllActiveByUser(AuthEntity.builder().uuid(userUuid).build());
    }

    @Query("""
            SELECT g
            FROM AuthPermissionGrantEntity g
            WHERE g.user = :user
              AND g.permission = :permission
              AND g.deletedAt IS NULL
            ORDER BY g.createdAt DESC, g.id DESC
            """)
    List<AuthPermissionGrantEntity> findAllActiveByUserAndPermission(@Param("user") AuthEntity user,
                                                                     @Param("permission") AuthPermissionEntity permission);

    default Optional<AuthPermissionGrantEntity> findActiveByUserUuidAndPermission(String userUuid, AuthPermissionEntity permission) {
        return findAllActiveByUserAndPermission(AuthEntity.builder().uuid(userUuid).build(), permission).stream().findFirst();
    }
}
