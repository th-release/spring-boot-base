package com.threlease.base.functions.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequestDto {
    @NotBlank
    private String identifier;
}
