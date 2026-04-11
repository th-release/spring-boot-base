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
    public AuthPermissionDto createPermission(AuthPermissionCreateDto dto) {
        AuthPermissionEntity parent = null;
        int depth = 1;
        if (dto.getParentCode() != null && !dto.getParentCode().isBlank()) {
            parent = authPermissionRepository.findActiveByCode(dto.getParentCode())
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
                .parentId(parent == null ? null : parent.getId())
                .sortOrder(dto.getSortOrder())
                .description(trim(dto.getDescription(), 255))
                .build();
        return toDto(authPermissionRepository.save(permission));
    }

    @Transactional
    public void grantPermission(String userUuid, String permissionCode, AuthEntity grantedBy) {
        AuthPermissionEntity permission = authPermissionRepository.findActiveByCode(permissionCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "권한을 찾을 수 없습니다."));
        if (authPermissionGrantRepository.findActiveByUserUuidAndPermissionId(userUuid, permission.getId()).isPresent()) {
            return;
        }
        authPermissionGrantRepository.save(AuthPermissionGrantEntity.builder()
                .userUuid(userUuid)
                .permissionId(permission.getId())
                .grantedByUuid(grantedBy == null ? null : grantedBy.getUuid())
                .build());
    }

    @Transactional
    public void revokePermission(String userUuid, String permissionCode) {
        AuthPermissionEntity permission = authPermissionRepository.findActiveByCode(permissionCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "권한을 찾을 수 없습니다."));
        authPermissionGrantRepository.findActiveByUserUuidAndPermissionId(userUuid, permission.getId())
                .ifPresent(grant -> {
                    grant.delete();
                    authPermissionGrantRepository.save(grant);
                });
    }

    @Transactional(readOnly = true)
    public List<AuthPermissionDto> getEffectivePermissions(String userUuid) {
        Set<Long> effectivePermissionIds = new HashSet<>();
        for (AuthPermissionGrantEntity grant : authPermissionGrantRepository.findAllActiveByUserUuid(userUuid)) {
            collectDescendantPermissionIds(grant.getPermissionId(), effectivePermissionIds);
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

        Optional<AuthPermissionEntity> target = authPermissionRepository.findActiveByCode(permissionCode);
        if (target.isEmpty()) {
            return false;
        }

        Set<Long> grantedPermissionIds = new HashSet<>();
        for (AuthPermissionGrantEntity grant : authPermissionGrantRepository.findAllActiveByUserUuid(user.getUuid())) {
            grantedPermissionIds.add(grant.getPermissionId());
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
        if (permission.getParentId() == null) {
            return null;
        }
        return authPermissionRepository.findById(permission.getParentId())
                .filter(parent -> !parent.isDeleted())
                .orElse(null);
    }

    private void collectDescendantPermissionIds(Long permissionId, Set<Long> collector) {
        if (permissionId == null || !collector.add(permissionId)) {
            return;
        }
        for (AuthPermissionEntity child : authPermissionRepository.findAllActiveByParentId(permissionId)) {
            collectDescendantPermissionIds(child.getId(), collector);
        }
    }

    private AuthPermissionDto toDto(AuthPermissionEntity permission) {
        return AuthPermissionDto.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .name(permission.getName())
                .depth(permission.getDepth())
                .parentId(permission.getParentId())
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
