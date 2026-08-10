package com.threlease.base.functions.auth;

import com.threlease.base.common.dto.SearchDto;
import com.threlease.base.common.entity.BaseEntity;
import com.threlease.base.common.enums.AuthStatuses;
import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.properties.app.auth.AuthSecurityProperties;
import com.threlease.base.common.utils.crypto.HashComponent;
import com.threlease.base.common.utils.random.RandomComponent;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.AuthLoginHistoryEntity;
import com.threlease.base.entities.AuthMfaEntity;
import com.threlease.base.functions.auth.dto.AdminUserSummaryDto;
import com.threlease.base.functions.auth.dto.AuthProfileDto;
import com.threlease.base.repositories.auth.AuthLoginHistoryRepository;
import com.threlease.base.repositories.auth.AuthMfaRepository;
import com.threlease.base.repositories.auth.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthUserService {
    private final AuthRepository authRepository;
    private final AuthLoginHistoryRepository authLoginHistoryRepository;
    private final AuthMfaRepository authMfaRepository;
    private final HashComponent hashComponent;
    private final RandomComponent randomComponent;
    private final AuthSecurityProperties authSecurityProperties;

    public Optional<AuthEntity> findOneByUUID(String uuid) {
        return Optional.ofNullable(findCachedUserByUUID(uuid));
    }

    public Optional<AuthEntity> findOneByUsername(String username) {
        return Optional.ofNullable(findCachedUserByUsername(username));
    }

    public Optional<AuthEntity> findOneByEmail(String email) {
        return authRepository.findOneByEmail(email);
    }

    public Optional<AuthEntity> findOneByIdentifier(String identifier) {
        return authRepository.findOneByUsernameOrEmail(identifier);
    }

    @Caching(evict = {
            @CacheEvict(value = "user", key = "#auth.uuid"),
            @CacheEvict(value = "user", key = "#auth.username")
    })
    public void authSave(AuthEntity auth) {
        authRepository.save(auth);
    }

    public void invalidateAccessTokens(AuthEntity auth) {
        if (auth == null) {
            return;
        }
        auth.setAccessTokenInvalidBefore(LocalDateTime.now());
        authSave(auth);
    }

    public void ensureLoginAllowed(AuthEntity auth) {
        if (auth.getStatus() == null || auth.getStatus() == AuthStatuses.WITHDRAWN || auth.getStatus() == AuthStatuses.SUSPENDED) {
            throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
        }
        if (auth.getStatus() == AuthStatuses.LOCKED) {
            LocalDateTime lockedUntil = getLockedUntil(auth);
            if (lockedUntil == null || lockedUntil.isAfter(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
            }
            auth.setStatus(AuthStatuses.ACTIVE);
            authSave(auth);
            saveLoginHistory(auth, false, 0, null, null, null, "LOCK_EXPIRED");
        }
        if (!authSecurityProperties.getLoginFailure().isEnabled()) {
            return;
        }
        LocalDateTime lockedUntil = getLockedUntil(auth);
        if (lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (lockedUntil != null) {
            saveLoginHistory(auth, false, 0, null, null, null, "LOCK_EXPIRED");
        }
    }

    public void assertTokenUsable(AuthEntity auth) {
        if (auth == null || auth.getStatus() == null || auth.getStatus() != AuthStatuses.ACTIVE) {
            throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
        }
    }

    public void recordFailedLogin(AuthEntity auth) {
        recordFailedLogin(auth, null, null, "WRONG_PASSWORD");
    }

    public void recordFailedLogin(AuthEntity auth, String clientIp, String userAgent, String failureReason) {
        if (auth == null) {
            return;
        }
        boolean failureLimitEnabled = authSecurityProperties.getLoginFailure().isEnabled();
        int failedLoginCount = failureLimitEnabled ? getFailedLoginCount(auth) + 1 : 0;
        LocalDateTime lockedUntil = failureLimitEnabled && failedLoginCount >= authSecurityProperties.getLoginFailure().getMaxAttempts()
                ? LocalDateTime.now().plusMinutes(authSecurityProperties.getLoginFailure().getLockMinutes())
                : null;
        if (lockedUntil != null) {
            auth.setStatus(AuthStatuses.LOCKED);
            authSave(auth);
        }
        saveLoginHistory(auth, false, failedLoginCount, lockedUntil, clientIp, userAgent, failureReason);
    }

    public void recordSuccessfulLogin(AuthEntity auth, String clientIp) {
        recordSuccessfulLogin(auth, clientIp, null);
    }

    public void recordSuccessfulLogin(AuthEntity auth, String clientIp, String userAgent) {
        if (auth.getStatus() == AuthStatuses.LOCKED) {
            auth.setStatus(AuthStatuses.ACTIVE);
            authSave(auth);
        }
        saveLoginHistory(auth, true, 0, null, clientIp, userAgent, null);
    }

    public void changePassword(AuthEntity auth, String encodedPassword, String salt) {
        auth.setPassword(encodedPassword);
        auth.setSalt(salt);
        authSave(auth);
    }

    public AuthService.PageResult<AdminUserSummaryDto> getUsers(SearchDto searchDto, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<AuthEntity> pageResult = authRepository.searchUsers(searchDto, pageable);

        return new AuthService.PageResult<>(
                pageResult.getContent().stream().map(this::toAdminUserSummary).toList(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    public Optional<AuthEntity> findManagedUserByUuid(String uuid) {
        return authRepository.findOneByUUID(uuid);
    }

    public void forceLockUser(AuthEntity auth, long minutes) {
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(Math.max(minutes, 1));
        auth.setStatus(AuthStatuses.LOCKED);
        authSave(auth);
        saveLoginHistory(auth, false, getFailedLoginCount(auth), lockedUntil, null, null, "ADMIN_LOCK");
    }

    public void unlockUser(AuthEntity auth) {
        auth.setStatus(AuthStatuses.ACTIVE);
        authSave(auth);
        saveLoginHistory(auth, false, 0, null, null, null, "ADMIN_UNLOCK");
    }

    public int getPasswordResetExpireMinutes() {
        return Math.max(1, authSecurityProperties.getPasswordReset().getCodeExpireMinutes());
    }

    public AuthProfileDto toAuthProfile(AuthEntity auth) {
        return AuthProfileDto.builder()
                .uuid(auth.getUuid())
                .username(auth.getUsername())
                .nickname(auth.getNickname())
                .email(auth.getEmail())
                .type(auth.getType())
                .status(auth.getStatus())
                .mfaGloballyEnabled(authSecurityProperties.getMfa().isEnabled())
                .mfaEnabled(isMfaEnabled(auth))
                .mfaEnrollmentRequired(authSecurityProperties.getMfa().isEnabled() && !isMfaEnabled(auth))
                .build();
    }

    public int getFailedLoginCount(AuthEntity auth) {
        return findLatestLoginHistory(auth)
                .map(AuthLoginHistoryEntity::getFailedLoginCount)
                .orElse(0);
    }

    public LocalDateTime getLockedUntil(AuthEntity auth) {
        return findLatestLoginHistory(auth)
                .map(AuthLoginHistoryEntity::getLockedUntil)
                .orElse(null);
    }

    public LocalDateTime getLastLoginAt(AuthEntity auth) {
        return findLatestSuccessfulLoginHistory(auth)
                .map(AuthLoginHistoryEntity::getCreatedAt)
                .orElse(null);
    }

    public String getLastLoginIp(AuthEntity auth) {
        return findLatestSuccessfulLoginHistory(auth)
                .map(AuthLoginHistoryEntity::getClientIp)
                .orElse(null);
    }

    private void saveLoginHistory(AuthEntity auth,
                                  boolean success,
                                  int failedLoginCount,
                                  LocalDateTime lockedUntil,
                                  String clientIp,
                                  String userAgent,
                                  String failureReason) {
        authLoginHistoryRepository.save(AuthLoginHistoryEntity.builder()
                .user(auth)
                .username(trim(auth.getUsername(), 24))
                .success(success)
                .failureReason(trim(failureReason, 120))
                .failedLoginCount(failedLoginCount)
                .lockedUntil(lockedUntil)
                .clientIp(trim(clientIp, 64))
                .userAgent(trim(userAgent, 512))
                .build());
    }

    @Cacheable(value = "user", key = "#uuid", unless = "#result == null")
    public AuthEntity findCachedUserByUUID(String uuid) {
        return authRepository.findOneByUUID(uuid).orElse(null);
    }

    @Cacheable(value = "user", key = "#username", unless = "#result == null")
    public AuthEntity findCachedUserByUsername(String username) {
        return authRepository.findOneByUsername(username).orElse(null);
    }

    private boolean isMfaEnabled(AuthEntity auth) {
        return authMfaRepository.findLatestActiveByUser(auth, PageRequest.of(0, 1)).stream().findFirst()
                .map(AuthMfaEntity::isEnabled)
                .orElse(false);
    }

    private Optional<AuthLoginHistoryEntity> findLatestLoginHistory(AuthEntity auth) {
        return authLoginHistoryRepository.findRecentByUser(auth, PageRequest.of(0, 1)).stream().findFirst();
    }

    private Optional<AuthLoginHistoryEntity> findLatestSuccessfulLoginHistory(AuthEntity auth) {
        return authLoginHistoryRepository.findRecentSuccessfulByUser(auth, PageRequest.of(0, 1)).stream().findFirst();
    }

    private AdminUserSummaryDto toAdminUserSummary(AuthEntity auth) {
        return AdminUserSummaryDto.builder()
                .uuid(auth.getUuid())
                .username(auth.getUsername())
                .nickname(auth.getNickname())
                .type(auth.getType())
                .status(auth.getStatus())
                .failedLoginCount(getFailedLoginCount(auth))
                .lockedUntil(getLockedUntil(auth))
                .lastLoginAt(getLastLoginAt(auth))
                .lastLoginIp(getLastLoginIp(auth))
                .mfaGloballyEnabled(authSecurityProperties.getMfa().isEnabled())
                .mfaEnabled(isMfaEnabled(auth))
                .mfaEnrollmentRequired(authSecurityProperties.getMfa().isEnabled() && !isMfaEnabled(auth))
                .build();
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.substring(0, Math.min(value.length(), maxLength));
    }
}
