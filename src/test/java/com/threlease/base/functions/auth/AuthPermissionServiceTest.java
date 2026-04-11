package com.threlease.base.functions.auth;

import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.AuthPermissionEntity;
import com.threlease.base.entities.AuthPermissionGrantEntity;
import com.threlease.base.functions.auth.dto.AuthPermissionCreateDto;
import com.threlease.base.repositories.auth.AuthPermissionGrantRepository;
import com.threlease.base.repositories.auth.AuthPermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthPermissionServiceTest {
    private AuthPermissionService authPermissionService;
    private AuthPermissionRepository permissionRepository;
    private AuthPermissionGrantRepository grantRepository;

    private final AuthPermissionEntity rootPermission = AuthPermissionEntity.builder()
            .uuid("permission-1")
            .code("SAMPLE_MENU")
            .name("샘플 대메뉴")
            .depth(1)
            .build();

    private final AuthPermissionEntity sectionPermission = AuthPermissionEntity.builder()
            .uuid("permission-2")
            .code("SAMPLE_MENU_SECTION")
            .name("샘플 중메뉴")
            .depth(2)
            .parent(rootPermission)
            .build();

    private final AuthPermissionEntity updatePermission = AuthPermissionEntity.builder()
            .uuid("permission-3")
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
        when(permissionRepository.findActiveByCode("SAMPLE_MENU_SECTION", org.springframework.data.domain.PageRequest.of(0, 1))).thenReturn(new PageImpl<>(List.of(sectionPermission)));
        when(permissionRepository.findActiveByCode("SAMPLE_MENU_SECTION_UPDATE", org.springframework.data.domain.PageRequest.of(0, 1))).thenReturn(new PageImpl<>(List.of(updatePermission)));
        when(permissionRepository.findAllActiveByParent(any(AuthPermissionEntity.class))).thenAnswer(invocation -> {
            AuthPermissionEntity parent = invocation.getArgument(0);
            if ("permission-1".equals(parent.getUuid())) {
                return List.of(sectionPermission);
            }
            if ("permission-2".equals(parent.getUuid())) {
                return List.of(updatePermission);
            }
            return List.of();
        });
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

    @Test
    void grantingParentPermissionCreatesChildGrants() {
        when(grantRepository.findLatestActiveByUserAndPermission(any(AuthEntity.class), any(AuthPermissionEntity.class), any()))
                .thenReturn(new PageImpl<>(List.of()));

        authPermissionService.grantPermission("actor-1", "SAMPLE_MENU", AuthEntity.builder().uuid("admin-1").build());

        verify(grantRepository, times(3)).save(any(AuthPermissionGrantEntity.class));
    }

    @Test
    void revokingParentPermissionRevokesChildGrants() {
        AuthEntity actor = AuthEntity.builder().uuid("actor-1").build();
        AuthPermissionGrantEntity rootGrant = AuthPermissionGrantEntity.builder()
                .user(actor)
                .permission(rootPermission)
                .build();
        AuthPermissionGrantEntity sectionGrant = AuthPermissionGrantEntity.builder()
                .user(actor)
                .permission(sectionPermission)
                .build();
        AuthPermissionGrantEntity updateGrant = AuthPermissionGrantEntity.builder()
                .user(actor)
                .permission(updatePermission)
                .build();

        when(grantRepository.findAllActiveByUser(any(AuthEntity.class))).thenReturn(List.of(rootGrant, sectionGrant, updateGrant));

        authPermissionService.revokePermission("actor-1", "SAMPLE_MENU");

        verify(grantRepository).delete(rootGrant);
        verify(grantRepository).delete(sectionGrant);
        verify(grantRepository).delete(updateGrant);
    }

    @Test
    void creatingChildPermissionGrantsItToParentPermissionHolders() {
        AuthEntity actor = AuthEntity.builder().uuid("actor-1").build();
        AuthPermissionGrantEntity parentGrant = AuthPermissionGrantEntity.builder()
                .user(actor)
                .permission(sectionPermission)
                .grantedBy(AuthEntity.builder().uuid("admin-1").build())
                .build();
        AuthPermissionCreateDto dto = new AuthPermissionCreateDto();
        dto.setCode("SAMPLE_MENU_SECTION_DELETE");
        dto.setName("샘플 삭제");
        dto.setParentCode("SAMPLE_MENU_SECTION");
        dto.setSortOrder(30);
        AuthPermissionEntity saved = AuthPermissionEntity.builder()
                .uuid("permission-4")
                .code("SAMPLE_MENU_SECTION_DELETE")
                .name("샘플 삭제")
                .depth(3)
                .parent(sectionPermission)
                .sortOrder(30)
                .build();

        when(permissionRepository.findActiveByCode("SAMPLE_MENU_SECTION_DELETE", org.springframework.data.domain.PageRequest.of(0, 1)))
                .thenReturn(new PageImpl<>(List.of()));
        when(permissionRepository.save(any(AuthPermissionEntity.class))).thenReturn(saved);
        when(grantRepository.findAllActiveByPermission(eq(sectionPermission))).thenReturn(List.of(parentGrant));
        when(grantRepository.findLatestActiveByUserAndPermission(any(AuthEntity.class), eq(saved), any()))
                .thenReturn(new PageImpl<>(List.of()));

        authPermissionService.createPermission(dto);

        verify(grantRepository).save(any(AuthPermissionGrantEntity.class));
    }

    @Test
    void systemAdminGrantCreatesAllActivePermissionGrants() {
        AuthPermissionEntity systemAdmin = AuthPermissionEntity.builder()
                .uuid("permission-99")
                .code(AuthPermissionService.SYSTEM_ADMIN)
                .name("시스템 관리자")
                .depth(1)
                .build();

        when(permissionRepository.findActiveByCode(AuthPermissionService.SYSTEM_ADMIN, org.springframework.data.domain.PageRequest.of(0, 1)))
                .thenReturn(new PageImpl<>(List.of(systemAdmin)));
        when(permissionRepository.findAllActive()).thenReturn(List.of(systemAdmin, rootPermission, sectionPermission, updatePermission));
        when(grantRepository.findLatestActiveByUserAndPermission(any(AuthEntity.class), any(AuthPermissionEntity.class), any()))
                .thenReturn(new PageImpl<>(List.of()));

        authPermissionService.grantPermission("admin-1", AuthPermissionService.SYSTEM_ADMIN, AuthEntity.builder().uuid("admin-1").build());

        verify(grantRepository, times(4)).save(any(AuthPermissionGrantEntity.class));
    }
}
