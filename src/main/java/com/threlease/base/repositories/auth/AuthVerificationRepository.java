package com.threlease.base.repositories.auth;

import com.threlease.base.common.enums.AuthVerificationType;
import com.threlease.base.entities.AuthVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthVerificationRepository extends JpaRepository<AuthVerificationEntity, Long> {
    Optional<AuthVerificationEntity> findTopByUserUuidAndTypeAndVerifiedFalseOrderByCreatedAtDesc(String userUuid, AuthVerificationType type);
    List<AuthVerificationEntity> findAllByUserUuidAndTypeAndVerifiedFalse(String userUuid, AuthVerificationType type);
    void deleteAllByUserUuidAndType(String userUuid, AuthVerificationType type);
}
