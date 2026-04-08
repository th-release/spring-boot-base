package com.threlease.base.functions.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FcmDeviceTokenDto {
    private Long id;
    private String deviceLabel;
    private String userAgent;
    private String lastIpAddress;
    private LocalDateTime lastUsedAt;
    private boolean enabled;
}
