package com.threlease.base.functions.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthPermissionGrantDto {
    @NotBlank
    @Size(max = 120)
    private String permissionCode;
}
