package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuthEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<AuthEntity, String>, AuthRepositoryCustom {
    @Query("SELECT u FROM AuthEntity u WHERE u.uuid = :uuid AND u.deletedAt IS NULL")
    Optional<AuthEntity> findOneByUUID(@Param("uuid") String uuid);

    @Query("SELECT u FROM AuthEntity u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    Page<AuthEntity> findByPagination(Pageable pageable);

    @Query("SELECT u FROM AuthEntity u WHERE LOWER(u.username) = LOWER(:username) AND u.deletedAt IS NULL")
    Optional<AuthEntity> findOneByUsername(@Param("username") String username);

    @Query("SELECT u FROM AuthEntity u WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL")
    Optional<AuthEntity> findOneByEmail(@Param("email") String email);

    @Query("""
            SELECT u
            FROM AuthEntity u
            WHERE (LOWER(u.username) = LOWER(:identifier)
               OR LOWER(u.email) = LOWER(:identifier))
              AND u.deletedAt IS NULL
            """)
    Optional<AuthEntity> findOneByUsernameOrEmail(@Param("identifier") String identifier);
}
