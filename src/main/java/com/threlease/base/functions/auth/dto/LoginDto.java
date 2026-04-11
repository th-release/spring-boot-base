package com.threlease.base.functions.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class LoginDto {
    @NotBlank
    @Size(max = 24)
    private String username;

    @NotBlank
    @Size(min = 8, max = 128)
    private String password;

    @Size(max = 12)
    private String otpCode;
}
