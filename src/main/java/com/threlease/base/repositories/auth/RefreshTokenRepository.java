package com.threlease.base.repositories.auth;

import com.threlease.base.entities.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByUserUuid(String userUuid);
    void deleteByUserUuid(String userUuid);
}
