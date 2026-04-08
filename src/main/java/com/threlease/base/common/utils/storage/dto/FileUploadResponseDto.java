package com.threlease.base.common.utils.storage.dto;

import com.threlease.base.common.utils.storage.entity.FileEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponseDto {
    private Long id;
    private String originalFileName;
    private String filePath;
    private String url;
    private String contentType;
    private Long fileSize;
    private String dirName;
    private String storageType;

    public static FileUploadResponseDto from(FileEntity fileEntity) {
        return FileUploadResponseDto.builder()
                .id(fileEntity.getId())
                .originalFileName(fileEntity.getOriginalFileName())
                .filePath(fileEntity.getFilePath())
                .url(fileEntity.getUrl())
                .contentType(fileEntity.getContentType())
                .fileSize(fileEntity.getFileSize())
                .dirName(fileEntity.getDirName())
                .storageType(fileEntity.getStorageType().name())
                .build();
    }
}
