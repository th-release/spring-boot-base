package com.threlease.base.functions.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RefreshTokenSessionDto {
    private String tokenId;
    private String familyId;
    private LocalDateTime expiryDate;
    private boolean current;
}
