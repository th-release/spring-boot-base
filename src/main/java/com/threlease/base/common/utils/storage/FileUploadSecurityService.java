package com.threlease.base.common.utils.storage;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.properties.storage.upload.UploadSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FileUploadSecurityService {
    private static final Pattern DIR_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9/_-]+$");
    private final UploadSecurityProperties uploadSecurityProperties;

    public void validate(MultipartFile file) {
        if (!uploadSecurityProperties.isEnabled()) {
            return;
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID);
        }
        if (file.getSize() > uploadSecurityProperties.getMaxFileSize().toBytes()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID, "허용된 파일 크기를 초과했습니다.");
        }

        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();

        if (uploadSecurityProperties.isBlockDoubleExtension() && originalFilename.chars().filter(ch -> ch == '.').count() > 1) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID, "다중 확장자 파일은 업로드할 수 없습니다.");
        }
    }

    public String sanitizeFilename(String originalFilename) {
        String sanitized = originalFilename == null ? "file" : originalFilename.replaceAll("[^A-Za-z0-9._-]", "_");
        return sanitized.length() > 120 ? sanitized.substring(sanitized.length() - 120) : sanitized;
    }

    public String validateDirName(String dirName) {
        if (dirName == null || dirName.isBlank()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID, "저장 디렉토리명이 비어 있습니다.");
        }

        String normalized = dirName.trim().replace('\\', '/');
        if (normalized.startsWith("/") || normalized.endsWith("/") || normalized.contains("..") || normalized.contains("//")) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID, "유효하지 않은 저장 디렉토리명입니다.");
        }
        if (!DIR_NAME_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID, "허용되지 않은 저장 디렉토리명입니다.");
        }
        return normalized;
    }
}
