package com.threlease.base.functions.auth;

import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.AuthPermissionEntity;
import com.threlease.base.entities.AuthPermissionGrantEntity;
import com.threlease.base.repositories.auth.AuthPermissionGrantRepository;
import com.threlease.base.repositories.auth.AuthPermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthPermissionServiceTest {
    private AuthPermissionService authPermissionService;
    private AuthPermissionRepository permissionRepository;
    private AuthPermissionGrantRepository grantRepository;

    private final AuthPermissionEntity rootPermission = AuthPermissionEntity.builder()
            .id(1L)
            .code("SAMPLE_MENU")
            .name("샘플 대메뉴")
            .depth(1)
            .build();

    private final AuthPermissionEntity sectionPermission = AuthPermissionEntity.builder()
            .id(2L)
            .code("SAMPLE_MENU_SECTION")
            .name("샘플 중메뉴")
            .depth(2)
            .parent(rootPermission)
            .build();

    private final AuthPermissionEntity updatePermission = AuthPermissionEntity.builder()
            .id(3L)
            .code("SAMPLE_MENU_SECTION_UPDATE")
            .name("샘플 수정")
            .depth(3)
            .parent(sectionPermission)
            .build();

    @BeforeEach
    void setUp() {
        permissionRepository = mock(AuthPermissionRepository.class);
        grantRepository = mock(AuthPermissionGrantRepository.class);
        authPermissionService = new AuthPermissionService(permissionRepository, grantRepository);

        when(permissionRepository.findActiveByCode("SAMPLE_MENU", org.springframework.data.domain.PageRequest.of(0, 1))).thenReturn(new PageImpl<>(List.of(rootPermission)));
        when(permissionRepository.findActiveByCode("SAMPLE_MENU_SECTION_UPDATE", org.springframework.data.domain.PageRequest.of(0, 1))).thenReturn(new PageImpl<>(List.of(updatePermission)));
    }

    @Test
    void parentGrantAllowsChildPermission() {
        AuthEntity actor = AuthEntity.builder().uuid("actor-1").build();
        AuthPermissionGrantEntity rootGrant = AuthPermissionGrantEntity.builder()
                .user(actor)
                .permission(rootPermission)
                .build();

        when(grantRepository.findAllActiveByUser(any(AuthEntity.class))).thenReturn(List.of(rootGrant));

        assertTrue(authPermissionService.hasPermission(actor, "SAMPLE_MENU_SECTION_UPDATE"));
    }

    @Test
    void missingParentGrantDeniesChildPermission() {
        AuthEntity actor = AuthEntity.builder().uuid("actor-1").build();

        when(grantRepository.findAllActiveByUser(any(AuthEntity.class))).thenReturn(List.of());

        assertFalse(authPermissionService.hasPermission(actor, "SAMPLE_MENU_SECTION_UPDATE"));
    }
}
