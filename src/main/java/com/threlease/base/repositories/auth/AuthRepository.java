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
public interface AuthRepository extends JpaRepository<AuthEntity, String> {
    @Query("SELECT u FROM AuthEntity u WHERE u.uuid = :uuid")
    Optional<AuthEntity> findOneByUUID(@Param("uuid") String uuid);

    @Query("SELECT u FROM AuthEntity u")
    Page<AuthEntity> findByPagination(Pageable pageable);

    @Query("SELECT u FROM AuthEntity u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<AuthEntity> findOneByUsername(@Param("username") String username);

    @Query("SELECT u FROM AuthEntity u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<AuthEntity> findOneByEmail(@Param("email") String email);

    @Query("""
            SELECT u
            FROM AuthEntity u
            WHERE LOWER(u.username) = LOWER(:identifier)
               OR LOWER(u.email) = LOWER(:identifier)
            """)
    Optional<AuthEntity> findOneByUsernameOrEmail(@Param("identifier") String identifier);

    @Query("""
            SELECT u
            FROM AuthEntity u
            WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<AuthEntity> findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCase(@Param("query") String query, Pageable pageable);
}
