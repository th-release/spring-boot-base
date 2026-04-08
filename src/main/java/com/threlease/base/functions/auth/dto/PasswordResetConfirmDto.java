package com.threlease.base.functions.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetConfirmDto {
    @NotBlank
    private String identifier;
    private String verificationCode;
    private String currentPassword;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmPassword;
}
