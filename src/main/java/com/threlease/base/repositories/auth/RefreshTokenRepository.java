package com.threlease.base.repositories.auth;

import com.threlease.base.entities.RefreshTokenEntity;
import com.threlease.base.entities.AuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.tokenId = :tokenId AND r.deletedAt IS NULL")
    Optional<RefreshTokenEntity> findByTokenId(@Param("tokenId") String tokenId);

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.tokenId = :tokenId AND r.user = :user AND r.deletedAt IS NULL")
    Optional<RefreshTokenEntity> findByTokenIdAndUser(@Param("tokenId") String tokenId, @Param("user") AuthEntity user);

    default Optional<RefreshTokenEntity> findByTokenIdAndUserUuid(String tokenId, String userUuid) {
        return findByTokenIdAndUser(tokenId, AuthEntity.builder().uuid(userUuid).build());
    }

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.familyId = :familyId AND r.deletedAt IS NULL")
    List<RefreshTokenEntity> findAllByFamilyId(@Param("familyId") String familyId);

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.user = :user AND r.revoked = false AND r.deletedAt IS NULL")
    List<RefreshTokenEntity> findAllByUserAndRevokedFalse(@Param("user") AuthEntity user);

    default List<RefreshTokenEntity> findAllByUserUuidAndRevokedFalse(String userUuid) {
        return findAllByUserAndRevokedFalse(AuthEntity.builder().uuid(userUuid).build());
    }
}
