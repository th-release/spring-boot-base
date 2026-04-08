package com.threlease.base.functions.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditLogDto {
    private Long id;
    private String actorUuid;
    private String action;
    private String resourceType;
    private String resourceId;
    private boolean success;
    private String clientIp;
    private String userAgent;
    private String detail;
    private LocalDateTime createdAt;
}
