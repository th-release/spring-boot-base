package com.threlease.base.functions.auth.dto;

import com.threlease.base.common.enums.Roles;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthProfileDto {
    private String uuid;
    private String username;
    private String nickname;
    private String email;
    private Roles role;
    private boolean mfaEnabled;
}
