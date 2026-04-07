package com.threlease.base.repositories.auth;

import com.threlease.base.entities.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenId(String tokenId);
    Optional<RefreshTokenEntity> findByTokenIdAndUserUuid(String tokenId, String userUuid);
    List<RefreshTokenEntity> findAllByFamilyId(String familyId);
    List<RefreshTokenEntity> findAllByUserUuidAndRevokedFalse(String userUuid);
    void deleteByTokenId(String tokenId);
    void deleteByFamilyId(String familyId);
}
