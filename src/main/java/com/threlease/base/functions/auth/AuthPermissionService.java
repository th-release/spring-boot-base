package com.threlease.base.functions.auth;

import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.AuthPermissionEntity;
import com.threlease.base.entities.AuthPermissionGrantEntity;
import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.functions.auth.dto.AuthPermissionCreateDto;
import com.threlease.base.functions.auth.dto.AuthPermissionDto;
import com.threlease.base.repositories.auth.AuthPermissionGrantRepository;
import com.threlease.base.repositories.auth.AuthPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthPermissionService {
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";

    private final AuthPermissionRepository authPermissionRepository;
    private final AuthPermissionGrantRepository authPermissionGrantRepository;

    @Transactional(readOnly = true)
    public List<AuthPermissionDto> getPermissions() {
        return authPermissionRepository.findAllActive().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AuthPermissionEntity ensureSystemAdminPermission() {
        return findActivePermission(SYSTEM_ADMIN)
                .orElseGet(() -> authPermissionRepository.save(AuthPermissionEntity.builder()
                        .code(SYSTEM_ADMIN)
                        .name("시스템 관리자")
                        .depth(1)
                        .parent(null)
                        .sortOrder(0)
                        .description("운영/관리자 API 전체 권한")
                        .build()));
    }

    @Transactional
    public AuthPermissionDto createPermission(AuthPermissionCreateDto dto) {
        AuthPermissionEntity parent = null;
        int depth = 1;
        if (dto.getParentCode() != null && !dto.getParentCode().isBlank()) {
            parent = findActivePermission(dto.getParentCode())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "상위 권한을 찾을 수 없습니다."));
            depth = parent.getDepth() + 1;
        }
        if (depth > 3) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "권한 뎁스는 최대 3단계까지만 허용됩니다.");
        }

        AuthPermissionEntity permission = AuthPermissionEntity.builder()
                .code(trim(dto.getCode(), 120))
                .name(trim(dto.getName(), 120))
                .depth(depth)
                .parent(parent)
                .sortOrder(dto.getSortOrder())
                .description(trim(dto.getDescription(), 255))
                .build();
        return toDto(authPermissionRepository.save(permission));
    }

    @Transactional
    public void grantPermission(String userUuid, String permissionCode, AuthEntity grantedBy) {
        AuthEntity user = AuthEntity.builder().uuid(userUuid).build();
        AuthPermissionEntity permission = findActivePermission(permissionCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "권한을 찾을 수 없습니다."));
        if (findActiveGrant(user, permission).isPresent()) {
            return;
        }
        authPermissionGrantRepository.save(AuthPermissionGrantEntity.builder()
                .user(user)
                .permission(permission)
                .grantedBy(grantedBy)
                .build());
    }

    @Transactional
    public void revokePermission(String userUuid, String permissionCode) {
        AuthEntity user = AuthEntity.builder().uuid(userUuid).build();
        AuthPermissionEntity permission = findActivePermission(permissionCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "권한을 찾을 수 없습니다."));
        findActiveGrant(user, permission)
                .ifPresent(grant -> {
                    grant.delete();
                    authPermissionGrantRepository.save(grant);
                });
    }

    @Transactional(readOnly = true)
    public List<AuthPermissionDto> getEffectivePermissions(String userUuid) {
        AuthEntity user = AuthEntity.builder().uuid(userUuid).build();
        Set<Long> effectivePermissionIds = new HashSet<>();
        for (AuthPermissionGrantEntity grant : authPermissionGrantRepository.findAllActiveByUser(user)) {
            collectDescendantPermissionIds(grant.getPermission().getId(), effectivePermissionIds);
        }

        List<AuthPermissionDto> result = new ArrayList<>();
        for (AuthPermissionEntity permission : authPermissionRepository.findAllActive()) {
            if (effectivePermissionIds.contains(permission.getId())) {
                result.add(toDto(permission));
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(AuthEntity user, String permissionCode) {
        if (user == null || permissionCode == null || permissionCode.isBlank()) {
            return false;
        }

        Optional<AuthPermissionEntity> target = findActivePermission(permissionCode);
        if (target.isEmpty()) {
            return false;
        }

        Set<Long> grantedPermissionIds = new HashSet<>();
        for (AuthPermissionGrantEntity grant : authPermissionGrantRepository.findAllActiveByUser(user)) {
            grantedPermissionIds.add(grant.getPermission().getId());
        }

        AuthPermissionEntity current = target.get();
        while (current != null) {
            if (grantedPermissionIds.contains(current.getId())) {
                return true;
            }
            current = resolveParent(current);
        }
        return false;
    }

    private AuthPermissionEntity resolveParent(AuthPermissionEntity permission) {
        return permission.getParent() != null && !permission.getParent().isDeleted() ? permission.getParent() : null;
    }

    private Optional<AuthPermissionEntity> findActivePermission(String code) {
        return authPermissionRepository.findActiveByCode(code, PageRequest.of(0, 1)).stream().findFirst();
    }

    private Optional<AuthPermissionGrantEntity> findActiveGrant(AuthEntity user, AuthPermissionEntity permission) {
        return authPermissionGrantRepository.findLatestActiveByUserAndPermission(user, permission, PageRequest.of(0, 1))
                .stream()
                .findFirst();
    }

    private void collectDescendantPermissionIds(Long permissionId, Set<Long> collector) {
        if (permissionId == null || !collector.add(permissionId)) {
            return;
        }
        AuthPermissionEntity parent = AuthPermissionEntity.builder().id(permissionId).build();
        for (AuthPermissionEntity child : authPermissionRepository.findAllActiveByParent(parent)) {
            collectDescendantPermissionIds(child.getId(), collector);
        }
    }

    private AuthPermissionDto toDto(AuthPermissionEntity permission) {
        return AuthPermissionDto.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .name(permission.getName())
                .depth(permission.getDepth())
                .parentId(permission.getParent() == null ? null : permission.getParent().getId())
                .sortOrder(permission.getSortOrder())
                .description(permission.getDescription())
                .build();
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.substring(0, Math.min(value.length(), maxLength));
    }
}
