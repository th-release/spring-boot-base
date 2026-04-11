package com.threlease.base.functions.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthPermissionCreateDto {
    @NotBlank
    @Size(max = 120)
    private String code;

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 120)
    private String parentCode;

    private int sortOrder;

    @Size(max = 255)
    private String description;
}
