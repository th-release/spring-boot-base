package com.threlease.base.functions.auth;

import com.threlease.base.common.enums.AuthStatuses;
import com.threlease.base.common.enums.AuthTypes;
import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.properties.app.admin.AdminProperties;
import com.threlease.base.entities.AuthEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthAdminBootstrap {
    private final AdminProperties adminProperties;
    private final AuthService authService;
    private final AuthPermissionService authPermissionService;
    private final AuthAccountFactory authAccountFactory;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void createInitialAdmin() {
        if (!adminProperties.isEnabled()) {
            return;
        }
        validateProperties();

        Optional<AuthEntity> existingAdmin = authService.findOneByUsername(adminProperties.getUsername());
        AuthEntity admin = existingAdmin.orElseGet(this::createAdmin);
        existingAdmin.ifPresent(this::resetPasswordIfConfigured);
        authPermissionService.ensureSystemAdminPermission();
        authPermissionService.grantPermission(admin.getUuid(), AuthPermissionService.SYSTEM_ADMIN, admin);

        if (existingAdmin.isPresent()) {
            log.info("Initial admin already exists. SYSTEM_ADMIN permission ensured for username={}", admin.getUsername());
        } else {
            log.info("Initial admin created. username={}", admin.getUsername());
        }
    }

    private void resetPasswordIfConfigured(AuthEntity admin) {
        if (!adminProperties.isResetPasswordOnStartup()) {
            return;
        }
        admin.setPassword(authAccountFactory.encodePassword(adminProperties.getPassword()));
        authService.authSave(admin);
        log.warn("Initial admin password was reset by app.admin.reset-password-on-startup=true. username={}", admin.getUsername());
    }

    private AuthEntity createAdmin() {
        if (adminProperties.getEmail() != null && !adminProperties.getEmail().isBlank()
                && authService.findOneByEmail(adminProperties.getEmail()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE, "초기 관리자 이메일이 이미 사용 중입니다.");
        }

        AuthEntity admin = authAccountFactory.create(
                adminProperties.getUsername(),
                adminProperties.getNickname(),
                adminProperties.getEmail(),
                adminProperties.getPassword(),
                AuthTypes.INTERNAL,
                AuthStatuses.ACTIVE
        );
        authService.authSave(admin);
        return admin;
    }

    private void validateProperties() {
        if (adminProperties.getUsername() == null || adminProperties.getUsername().isBlank()) {
            throw new IllegalStateException("app.admin.enabled=true requires app.admin.username");
        }
        if (adminProperties.getPassword() == null || adminProperties.getPassword().isBlank()) {
            throw new IllegalStateException("app.admin.enabled=true requires app.admin.password");
        }
    }

}
