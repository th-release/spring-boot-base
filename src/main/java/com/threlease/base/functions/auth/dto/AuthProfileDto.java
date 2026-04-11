package com.threlease.base.functions.auth.dto;

import com.threlease.base.common.enums.AuthStatuses;
import com.threlease.base.common.enums.AuthTypes;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthProfileDto {
    private String uuid;
    private String username;
    private String nickname;
    private String email;
    private AuthTypes type;
    private AuthStatuses status;
    private boolean mfaGloballyEnabled;
    private boolean mfaEnabled;
    private boolean mfaEnrollmentRequired;
}
