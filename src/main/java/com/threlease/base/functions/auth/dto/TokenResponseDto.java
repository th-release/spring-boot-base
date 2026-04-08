package com.threlease.base.functions.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TokenResponseDto {
    private String accessToken;
    private String refreshToken;
    private boolean mfaEnabled;
    private boolean mfaEnrollmentRequired;
}
