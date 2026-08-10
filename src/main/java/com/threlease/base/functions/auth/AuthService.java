package com.threlease.base.functions.auth;

import com.threlease.base.common.dto.SearchDto;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.dto.AdminUserSummaryDto;
import com.threlease.base.functions.auth.dto.AuthProfileDto;
import com.threlease.base.functions.auth.dto.RefreshTokenSessionDto;
import com.threlease.base.functions.auth.dto.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthUserService authUserService;
    private final AuthSessionService authSessionService;

    public Optional<AuthEntity> findOneByUUID(String uuid) {
        return authUserService.findOneByUUID(uuid);
    }

    public Optional<AuthEntity> findOneByUsername(String username) {
        return authUserService.findOneByUsername(username);
    }

    public Optional<AuthEntity> findOneByEmail(String email) {
        return authUserService.findOneByEmail(email);
    }

    public Optional<AuthEntity> findOneByIdentifier(String identifier) {
        return authUserService.findOneByIdentifier(identifier);
    }

    public void authSave(AuthEntity auth) {
        authUserService.authSave(auth);
    }

    public void invalidateAccessTokens(AuthEntity auth) {
        authUserService.invalidateAccessTokens(auth);
    }

    public void ensureLoginAllowed(AuthEntity auth) {
        authUserService.ensureLoginAllowed(auth);
    }

    public void assertTokenUsable(AuthEntity auth) {
        authUserService.assertTokenUsable(auth);
    }

    public void recordFailedLogin(AuthEntity auth) {
        authUserService.recordFailedLogin(auth);
    }

    public void recordFailedLogin(AuthEntity auth, String clientIp, String userAgent, String failureReason) {
        authUserService.recordFailedLogin(auth, clientIp, userAgent, failureReason);
        if (auth != null && auth.getStatus() == com.threlease.base.common.enums.AuthStatuses.LOCKED) {
            authSessionService.logoutAll(auth.getUuid());
        }
    }

    public void recordSuccessfulLogin(AuthEntity auth, String clientIp) {
        authUserService.recordSuccessfulLogin(auth, clientIp);
    }

    public void recordSuccessfulLogin(AuthEntity auth, String clientIp, String userAgent) {
        authUserService.recordSuccessfulLogin(auth, clientIp, userAgent);
    }

    public void changePassword(AuthEntity auth, String encodedPassword, String salt) {
        authUserService.changePassword(auth, encodedPassword, salt);
    }

    public TokenResponseDto issueTokens(AuthEntity user) {
        return authSessionService.issueTokens(user);
    }

    public TokenResponseDto issueTokens(AuthEntity user, String familyId) {
        return authSessionService.issueTokens(user, familyId);
    }

    public TokenResponseDto issueTokens(AuthEntity user, String userAgent, String ipAddress) {
        return authSessionService.issueTokens(user, userAgent, ipAddress);
    }

    public TokenResponseDto issueTokens(AuthEntity user, String familyId, String userAgent, String ipAddress) {
        return authSessionService.issueTokens(user, familyId, userAgent, ipAddress);
    }

    public TokenResponseDto refresh(String refreshToken, String userAgent, String ipAddress) {
        return authSessionService.refresh(refreshToken, userAgent, ipAddress);
    }

    public void logout(String refreshToken, String userUuid) {
        authSessionService.logout(refreshToken, userUuid, null);
    }

    public void logout(String refreshToken, String userUuid, String accessToken) {
        authSessionService.logout(refreshToken, userUuid, accessToken);
    }

    public void logoutAll(String userUuid) {
        authSessionService.logoutAll(userUuid);
    }

    public void revokeSession(String userUuid, String tokenId) {
        authSessionService.revokeSession(userUuid, tokenId);
    }

    public List<RefreshTokenSessionDto> getSessions(String userUuid, String currentRefreshToken) {
        return authSessionService.getSessions(userUuid, currentRefreshToken);
    }

    public List<RefreshTokenSessionDto> getSessionsForUser(String userUuid) {
        return authSessionService.getSessionsForUser(userUuid);
    }

    public Optional<AuthEntity> findOneByToken(String token) {
        return authSessionService.findOneByToken(token);
    }

    public AuthService.PageResult<AdminUserSummaryDto> getUsers(SearchDto searchDto, Pageable pageable) {
        return authUserService.getUsers(searchDto, pageable);
    }

    public Optional<AuthEntity> findManagedUserByUuid(String uuid) {
        return authUserService.findManagedUserByUuid(uuid);
    }

    public void forceLockUser(AuthEntity auth, long minutes) {
        authUserService.forceLockUser(auth, minutes);
        if (auth != null) {
            authSessionService.logoutAll(auth.getUuid());
        }
    }

    public void unlockUser(AuthEntity auth) {
        authUserService.unlockUser(auth);
    }

    public int getPasswordResetExpireMinutes() {
        return authUserService.getPasswordResetExpireMinutes();
    }

    public AuthProfileDto toAuthProfile(AuthEntity auth) {
        return authUserService.toAuthProfile(auth);
    }

    public int getFailedLoginCount(AuthEntity auth) {
        return authUserService.getFailedLoginCount(auth);
    }

    public LocalDateTime getLockedUntil(AuthEntity auth) {
        return authUserService.getLockedUntil(auth);
    }

    public LocalDateTime getLastLoginAt(AuthEntity auth) {
        return authUserService.getLastLoginAt(auth);
    }

    public String getLastLoginIp(AuthEntity auth) {
        return authUserService.getLastLoginIp(auth);
    }

    public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    }
}
