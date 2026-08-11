package com.threlease.base.functions.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MfaRegisterDto {
    @NotBlank
    @Size(max = 12)
    private String otpCode;
}
