package com.threlease.base.functions.auth;

import com.threlease.base.common.enums.Roles;
import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.dto.AdminUserSummaryDto;
import com.threlease.base.functions.auth.dto.AuditLogDto;
import com.threlease.base.functions.auth.dto.RefreshTokenSessionDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthAdminService {
    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final MfaService mfaService;

    public AuthEntity assertAdmin(AuthEntity user) {
        if (user == null || user.getRole() != Roles.ROLE_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return user;
    }

    public AuthService.PageResult<AdminUserSummaryDto> getUsers(AuthEntity admin, String query, int page, int size) {
        assertAdmin(admin);
        return authService.getUsers(query, page, size);
    }

    public List<RefreshTokenSessionDto> getUserSessions(AuthEntity admin, String uuid) {
        assertAdmin(admin);
        return authService.getSessionsForUser(uuid);
    }

    public void logoutAll(AuthEntity admin, String uuid, HttpServletRequest request) {
        assertAdmin(admin);
        authService.logoutAll(uuid);
        auditLogService.logAdmin(admin.getUuid(), "ADMIN_LOGOUT_ALL", "AUTH", uuid, true, request, "Admin revoked all sessions");
    }

    public void lockUser(AuthEntity admin, String uuid, long minutes, HttpServletRequest request) {
        assertAdmin(admin);
        AuthEntity target = authService.findManagedUserByUuid(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        authService.forceLockUser(target, minutes);
        auditLogService.logAdmin(admin.getUuid(), "ADMIN_LOCK_USER", "AUTH", uuid, true, request, "User locked for " + minutes + " minutes");
    }

    public void unlockUser(AuthEntity admin, String uuid, HttpServletRequest request) {
        assertAdmin(admin);
        AuthEntity target = authService.findManagedUserByUuid(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        target.setLockedUntil(null);
        target.setFailedLoginCount(0);
        authService.authSave(target);
        auditLogService.logAdmin(admin.getUuid(), "ADMIN_UNLOCK_USER", "AUTH", uuid, true, request, "User unlocked");
    }

    public void resetUserMfa(AuthEntity admin, String uuid, HttpServletRequest request) {
        assertAdmin(admin);
        AuthEntity target = authService.findManagedUserByUuid(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        mfaService.reset(target);
        auditLogService.logAdmin(admin.getUuid(), "ADMIN_RESET_MFA", "AUTH", uuid, true, request, "User MFA reset");
    }

    public Page<AuditLogDto> getAuditLogs(AuthEntity admin, int page, int size) {
        assertAdmin(admin);
        return auditLogService.getAuditLogs(PageRequest.of(page, size));
    }
}
