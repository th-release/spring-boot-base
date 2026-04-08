package com.threlease.base.functions.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class FcmPushRequestDto {
    @NotBlank
    private String title;
    @NotBlank
    private String body;
    private Map<String, String> data;
}
