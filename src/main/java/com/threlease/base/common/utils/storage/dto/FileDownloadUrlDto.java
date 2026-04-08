package com.threlease.base.common.utils.storage.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileDownloadUrlDto {
    private Long id;
    private String fileName;
    private String storageType;
    private String downloadUrl;
    private LocalDateTime expiresAt;
}
