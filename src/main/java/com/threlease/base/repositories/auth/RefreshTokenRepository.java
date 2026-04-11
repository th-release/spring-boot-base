package com.threlease.base.repositories.auth;

import com.threlease.base.entities.RefreshTokenEntity;
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

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.tokenId = :tokenId AND r.userUuid = :userUuid AND r.deletedAt IS NULL")
    Optional<RefreshTokenEntity> findByTokenIdAndUserUuid(@Param("tokenId") String tokenId, @Param("userUuid") String userUuid);

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.familyId = :familyId AND r.deletedAt IS NULL")
    List<RefreshTokenEntity> findAllByFamilyId(@Param("familyId") String familyId);

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.userUuid = :userUuid AND r.revoked = false AND r.deletedAt IS NULL")
    List<RefreshTokenEntity> findAllByUserUuidAndRevokedFalse(@Param("userUuid") String userUuid);
}
