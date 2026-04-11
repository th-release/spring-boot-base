package com.threlease.base.functions.auth;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.dto.AdminUserSummaryDto;
import com.threlease.base.functions.auth.dto.AuthPermissionCreateDto;
import com.threlease.base.functions.auth.dto.AuthPermissionDto;
import com.threlease.base.functions.auth.dto.RefreshTokenSessionDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthAdminService {
    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final MfaService mfaService;
    private final AuthPermissionService authPermissionService;

    public AuthEntity assertAdmin(AuthEntity user) {
        if (!authPermissionService.hasPermission(user, AuthPermissionService.SYSTEM_ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return user;
    }

    public AuthService.PageResult<AdminUserSummaryDto> getUsers(AuthEntity admin, String query, Pageable pageable) {
        assertAdmin(admin);
        return authService.getUsers(query, pageable);
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
        authService.unlockUser(target);
        auditLogService.logAdmin(admin.getUuid(), "ADMIN_UNLOCK_USER", "AUTH", uuid, true, request, "User unlocked");
    }

    public void resetUserMfa(AuthEntity admin, String uuid, HttpServletRequest request) {
        assertAdmin(admin);
        AuthEntity target = authService.findManagedUserByUuid(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        mfaService.reset(target);
        auditLogService.logAdmin(admin.getUuid(), "ADMIN_RESET_MFA", "AUTH", uuid, true, request, "User MFA reset");
    }

    public List<AuthPermissionDto> getPermissions(AuthEntity admin) {
        assertAdmin(admin);
        return authPermissionService.getPermissions();
    }

    public AuthPermissionDto createPermission(AuthEntity admin, AuthPermissionCreateDto dto, HttpServletRequest request) {
        assertAdmin(admin);
        AuthPermissionDto permission = authPermissionService.createPermission(dto);
        auditLogService.logAdmin(admin.getUuid(), "ADMIN_CREATE_PERMISSION", "AUTH_PERMISSION", permission.getCode(), true, request, "Permission created");
        return permission;
    }

    public void grantPermission(AuthEntity admin, String uuid, String permissionCode, HttpServletRequest request) {
        assertAdmin(admin);
        authPermissionService.grantPermission(uuid, permissionCode, admin);
        auditLogService.logAdmin(admin.getUuid(), "ADMIN_GRANT_PERMISSION", "AUTH_PERMISSION", uuid, true, request, "Permission granted: " + permissionCode);
    }

    public void revokePermission(AuthEntity admin, String uuid, String permissionCode, HttpServletRequest request) {
        assertAdmin(admin);
        authPermissionService.revokePermission(uuid, permissionCode);
        auditLogService.logAdmin(admin.getUuid(), "ADMIN_REVOKE_PERMISSION", "AUTH_PERMISSION", uuid, true, request, "Permission revoked: " + permissionCode);
    }

    public List<AuthPermissionDto> getEffectivePermissions(AuthEntity admin, String uuid) {
        assertAdmin(admin);
        return authPermissionService.getEffectivePermissions(uuid);
    }
}
