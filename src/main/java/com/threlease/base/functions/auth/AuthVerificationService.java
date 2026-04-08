package com.threlease.base.functions.auth;

import com.threlease.base.common.enums.AuthVerificationType;
import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.utils.crypto.HashComponent;
import com.threlease.base.common.utils.random.RandomComponent;
import com.threlease.base.entities.AuthVerificationEntity;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.repositories.auth.AuthVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthVerificationService {
    private final AuthVerificationRepository authVerificationRepository;
    private final HashComponent hashComponent;
    private final RandomComponent randomComponent;

    public String issueCode(AuthEntity auth, AuthVerificationType type, String target, int expireMinutes) {
        authVerificationRepository.deleteAll(authVerificationRepository.findAllByUserUuidAndType(auth.getUuid(), type));
        String code = randomComponent.generateOtp(6);
        authVerificationRepository.save(AuthVerificationEntity.builder()
                .userUuid(auth.getUuid())
                .type(type)
                .target(target)
                .verificationHash(hashComponent.generateSHA256(code))
                .expiresAt(LocalDateTime.now().plusMinutes(Math.max(1, expireMinutes)))
                .verified(false)
                .build());
        return code;
    }

    public void verifyCode(AuthEntity auth, AuthVerificationType type, String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID);
        }

        AuthVerificationEntity verification = authVerificationRepository
                .findTopByUserUuidAndTypeAndVerifiedFalseOrderByCreatedAtDesc(auth.getUuid(), type)
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_CODE_EXPIRED);
        }

        String providedHash = hashComponent.generateSHA256(code);
        if (!providedHash.equals(verification.getVerificationHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID);
        }

        verification.setVerified(true);
        authVerificationRepository.save(verification);
    }

    public void clear(AuthEntity auth, AuthVerificationType type) {
        authVerificationRepository.deleteAll(authVerificationRepository.findAllByUserUuidAndType(auth.getUuid(), type));
    }
}
