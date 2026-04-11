package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuthMfaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuthMfaRepository extends JpaRepository<AuthMfaEntity, Long> {
    @Query("""
            SELECT m
            FROM AuthMfaEntity m
            WHERE m.userUuid = :userUuid
              AND m.deletedAt IS NULL
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<AuthMfaEntity> findAllActiveByUserUuid(@Param("userUuid") String userUuid);

    default Optional<AuthMfaEntity> findActiveByUserUuid(String userUuid) {
        return findAllActiveByUserUuid(userUuid).stream().findFirst();
    }
}
