package com.threlease.base.functions.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MfaSetupResponseDto {
    private String secret;
    private String otpAuthUri;
    private String qrCodeBase64;
}
