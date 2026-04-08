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
    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.tokenId = :tokenId")
    Optional<RefreshTokenEntity> findByTokenId(String tokenId);

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.tokenId = :tokenId AND r.userUuid = :userUuid")
    Optional<RefreshTokenEntity> findByTokenIdAndUserUuid(String tokenId, String userUuid);

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.familyId = :familyId")
    List<RefreshTokenEntity> findAllByFamilyId(String familyId);

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.userUuid = :userUuid AND r.revoked = false")
    List<RefreshTokenEntity> findAllByUserUuidAndRevokedFalse(String userUuid);
}
