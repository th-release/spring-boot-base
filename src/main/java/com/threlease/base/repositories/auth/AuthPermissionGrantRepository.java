package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuthPermissionGrantEntity;
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
            WHERE g.userUuid = :userUuid
              AND g.deletedAt IS NULL
              AND p.deletedAt IS NULL
            ORDER BY p.depth ASC, p.sortOrder ASC, p.id ASC
            """)
    List<AuthPermissionGrantEntity> findAllActiveByUserUuid(@Param("userUuid") String userUuid);

    @Query("""
            SELECT g
            FROM AuthPermissionGrantEntity g
            WHERE g.userUuid = :userUuid
              AND g.permissionId = :permissionId
              AND g.deletedAt IS NULL
            ORDER BY g.createdAt DESC, g.id DESC
            """)
    List<AuthPermissionGrantEntity> findAllActiveByUserUuidAndPermissionId(@Param("userUuid") String userUuid,
                                                                           @Param("permissionId") Long permissionId);

    default Optional<AuthPermissionGrantEntity> findActiveByUserUuidAndPermissionId(String userUuid, Long permissionId) {
        return findAllActiveByUserUuidAndPermissionId(userUuid, permissionId).stream().findFirst();
    }
}
