package com.threlease.base.functions.auth.dto;

import com.threlease.base.common.enums.Roles;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserSummaryDto {
    private String uuid;
    private String username;
    private String nickname;
    private Roles role;
    private int failedLoginCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private boolean mfaEnabled;
}
