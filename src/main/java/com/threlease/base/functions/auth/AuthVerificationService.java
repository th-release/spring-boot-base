package com.threlease.base.functions.auth;

import com.threlease.base.common.enums.AuthVerificationType;
import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.properties.app.auth.AuthSecurityProperties;
import com.threlease.base.common.utils.crypto.HashComponent;
import com.threlease.base.common.utils.random.RandomComponent;
import com.threlease.base.entities.AuthVerificationEntity;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.repositories.auth.AuthVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthVerificationService {
    private final AuthVerificationRepository authVerificationRepository;
    private final HashComponent hashComponent;
    private final RandomComponent randomComponent;
    private final AuthSecurityProperties authSecurityProperties;

    public String issueCode(AuthEntity auth, AuthVerificationType type, String target, int expireMinutes) {
        authVerificationRepository.deleteAll(authVerificationRepository.findAllByUserAndType(auth, type));
        String code = randomComponent.generateOtp(6);
        authVerificationRepository.save(AuthVerificationEntity.builder()
                .user(auth)
                .type(type)
                .target(target)
                .verificationHash(hashComponent.generateSHA256(code))
                .expiresAt(LocalDateTime.now().plusMinutes(Math.max(1, expireMinutes)))
                .failedAttempts(0)
                .lockedUntil(null)
                .verified(false)
                .build());
        return code;
    }

    public void verifyCode(AuthEntity auth, AuthVerificationType type, String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID);
        }

        AuthVerificationEntity verification = authVerificationRepository
                .findLatestByUserAndTypeAndVerifiedFalse(auth, type, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_CODE_EXPIRED);
        }
        if (verification.getLockedUntil() != null && verification.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID);
        }

        String providedHash = hashComponent.generateSHA256(code);
        if (!providedHash.equals(verification.getVerificationHash())) {
            recordFailedAttempt(verification);
            throw new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID);
        }

        verification.setVerified(true);
        authVerificationRepository.save(verification);
    }

    public void clear(AuthEntity auth, AuthVerificationType type) {
        authVerificationRepository.deleteAll(authVerificationRepository.findAllByUserAndType(auth, type));
    }

    private void recordFailedAttempt(AuthVerificationEntity verification) {
        int failedAttempts = verification.getFailedAttempts() + 1;
        verification.setFailedAttempts(failedAttempts);

        int maxAttempts = Math.max(1, authSecurityProperties.getPasswordReset().getMaxVerificationAttempts());
        if (failedAttempts >= maxAttempts) {
            long lockMinutes = Math.max(1, authSecurityProperties.getPasswordReset().getVerificationLockMinutes());
            verification.setLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
        }

        authVerificationRepository.save(verification);
    }
}
