package com.threlease.base.functions.auth;

import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.AuthPermissionEntity;
import com.threlease.base.entities.AuthPermissionGrantEntity;
import com.threlease.base.repositories.auth.AuthPermissionGrantRepository;
import com.threlease.base.repositories.auth.AuthPermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthPermissionServiceTest {
    private AuthPermissionService authPermissionService;
    private AuthPermissionRepository permissionRepository;
    private AuthPermissionGrantRepository grantRepository;

    private final AuthPermissionEntity studentRoot = AuthPermissionEntity.builder()
            .id(1L)
            .code("STUDENT_MANAGE")
            .name("학생관리")
            .depth(1)
            .build();

    private final AuthPermissionEntity studentMenu = AuthPermissionEntity.builder()
            .id(2L)
            .code("STUDENT_MANAGE_STUDENT")
            .name("학생관리")
            .depth(2)
            .parentId(1L)
            .build();

    private final AuthPermissionEntity editStudent = AuthPermissionEntity.builder()
            .id(3L)
            .code("STUDENT_MANAGE_STUDENT_EDIT")
            .name("학생 정보 수정")
            .depth(3)
            .parentId(2L)
            .build();

    @BeforeEach
    void setUp() {
        permissionRepository = mock(AuthPermissionRepository.class);
        grantRepository = mock(AuthPermissionGrantRepository.class);
        authPermissionService = new AuthPermissionService(permissionRepository, grantRepository);

        when(permissionRepository.findActiveByCode("STUDENT_MANAGE")).thenReturn(Optional.of(studentRoot));
        when(permissionRepository.findActiveByCode("STUDENT_MANAGE_STUDENT_EDIT")).thenReturn(Optional.of(editStudent));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(studentRoot));
        when(permissionRepository.findById(2L)).thenReturn(Optional.of(studentMenu));
        when(permissionRepository.findById(3L)).thenReturn(Optional.of(editStudent));
    }

    @Test
    void parentGrantAllowsChildPermission() {
        AuthEntity teacher = AuthEntity.builder().uuid("teacher-1").build();
        AuthPermissionGrantEntity rootGrant = AuthPermissionGrantEntity.builder()
                .userUuid("teacher-1")
                .permissionId(1L)
                .build();

        when(grantRepository.findAllActiveByUserUuid("teacher-1")).thenReturn(List.of(rootGrant));

        assertTrue(authPermissionService.hasPermission(teacher, "STUDENT_MANAGE_STUDENT_EDIT"));
    }

    @Test
    void missingParentGrantDeniesChildPermission() {
        AuthEntity teacher = AuthEntity.builder().uuid("teacher-1").build();

        when(grantRepository.findAllActiveByUserUuid("teacher-1")).thenReturn(List.of());

        assertFalse(authPermissionService.hasPermission(teacher, "STUDENT_MANAGE_STUDENT_EDIT"));
    }
}
