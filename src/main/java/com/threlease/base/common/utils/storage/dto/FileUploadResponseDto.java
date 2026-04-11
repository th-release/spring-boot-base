package com.threlease.base.common.utils.storage.dto;

import com.threlease.base.common.utils.storage.entity.FileEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponseDto {
    private String uuid;
    private String originalFileName;
    private String filePath;
    private String url;
    private String contentType;
    private Long fileSize;
    private String dirName;
    private String storageType;

    public static FileUploadResponseDto from(FileEntity fileEntity) {
        return FileUploadResponseDto.builder()
                .uuid(fileEntity.getUuid())
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
