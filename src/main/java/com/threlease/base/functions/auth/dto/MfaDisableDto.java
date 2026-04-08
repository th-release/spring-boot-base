package com.threlease.base.functions.auth.dto;

import lombok.Data;

@Data
public class MfaDisableDto {
    private String password;
    private String otpCode;
}
