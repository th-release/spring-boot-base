package com.threlease.base.functions.auth.dto;

import com.threlease.base.common.enums.AuthStatuses;
import com.threlease.base.common.enums.UserTypes;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserSummaryDto {
    private String uuid;
    private String username;
    private String nickname;
    private UserTypes type;
    private AuthStatuses status;
    private int failedLoginCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private boolean mfaGloballyEnabled;
    private boolean mfaEnabled;
    private boolean mfaEnrollmentRequired;
}
