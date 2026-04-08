package com.threlease.base.functions.auth.dto;

import lombok.Data;

@Data
public class MfaRegisterDto {
    private String otpCode;
}
