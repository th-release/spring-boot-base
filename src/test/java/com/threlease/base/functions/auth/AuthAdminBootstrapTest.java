package com.threlease.base.functions.auth;

import com.threlease.base.common.enums.AuthStatuses;
import com.threlease.base.common.enums.AuthTypes;
import com.threlease.base.common.properties.app.admin.AdminProperties;
import com.threlease.base.entities.AuthEntity;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthAdminBootstrapTest {

    @Test
    void resetPasswordOnStartupInvalidatesExistingSessions() {
        AdminProperties adminProperties = new AdminProperties();
        adminProperties.setEnabled(true);
        adminProperties.setUsername("admin");
        adminProperties.setPassword("Admin1234!");
        adminProperties.setNickname("관리자");
        adminProperties.setResetPasswordOnStartup(true);

        AuthService authService = mock(AuthService.class);
        AuthPermissionService authPermissionService = mock(AuthPermissionService.class);
        AuthAccountFactory authAccountFactory = mock(AuthAccountFactory.class);

        AuthEntity existingAdmin = AuthEntity.builder()
                .uuid("admin-1")
                .username("admin")
                .nickname("관리자")
                .password("old")
                .salt("old-salt")
                .type(AuthTypes.INTERNAL)
                .status(AuthStatuses.ACTIVE)
                .build();

        when(authService.findOneByUsername("admin")).thenReturn(java.util.Optional.of(existingAdmin));
        when(authAccountFactory.encodePassword("Admin1234!")).thenReturn(new AuthPasswordService.EncodedPassword("new-hash", "new-salt"));

        AuthAdminBootstrap bootstrap = new AuthAdminBootstrap(adminProperties, authService, authPermissionService, authAccountFactory);
        bootstrap.createInitialAdmin();

        verify(authService).authSave(existingAdmin);
        verify(authService).logoutAll("admin-1");
        verify(authPermissionService).ensureSystemAdminHasAllPermissions(existingAdmin);
    }
}
