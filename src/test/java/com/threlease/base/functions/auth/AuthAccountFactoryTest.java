package com.threlease.base.functions.auth;

import com.threlease.base.common.enums.AuthStatuses;
import com.threlease.base.common.enums.AuthTypes;
import com.threlease.base.entities.AuthEntity;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthAccountFactoryTest {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthAccountFactory authAccountFactory = new AuthAccountFactory(passwordEncoder);

    @Test
    void createEncodesPasswordWithLoginCompatibleEncoder() {
        AuthEntity admin = authAccountFactory.create(
                "admin",
                "관리자",
                "admin@base.local",
                "Admin1234!",
                AuthTypes.INTERNAL,
                AuthStatuses.ACTIVE
        );

        assertNotEquals("Admin1234!", admin.getPassword());
        assertTrue(passwordEncoder.matches("Admin1234!", admin.getPassword()));
        assertEquals(AuthTypes.INTERNAL, admin.getType());
        assertEquals(AuthStatuses.ACTIVE, admin.getStatus());
    }
}
