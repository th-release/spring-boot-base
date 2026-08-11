package com.threlease.base.functions.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmDeviceTokenRequestDto {
    @NotBlank
    private String deviceToken;

    @Size(max = 120)
    private String deviceLabel;
}
