package com.threlease.base.functions.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FcmPushResultDto {
    private List<String> messageIds;
    private List<String> failedTokenUuids;
    private int successCount;
    private int failureCount;
}
