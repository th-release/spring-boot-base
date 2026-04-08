package com.threlease.base.common.utils.storage;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.properties.storage.upload.UploadSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FileUploadSecurityService {
    private final UploadSecurityProperties uploadSecurityProperties;

    public void validate(MultipartFile file) {
        if (!uploadSecurityProperties.isEnabled()) {
            return;
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID);
        }
        if (file.getSize() > uploadSecurityProperties.getMaxFileSizeBytes()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID, "허용된 파일 크기를 초과했습니다.");
        }

        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = resolveExtension(originalFilename);
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);

        if (uploadSecurityProperties.isBlockDoubleExtension() && originalFilename.chars().filter(ch -> ch == '.').count() > 1) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID, "다중 확장자 파일은 업로드할 수 없습니다.");
        }
        if (!uploadSecurityProperties.getAllowedExtensions().contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID, "허용되지 않은 파일 확장자입니다.");
        }
        if (!uploadSecurityProperties.getAllowedContentTypes().contains(contentType)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_INVALID, "허용되지 않은 파일 유형입니다.");
        }
    }

    public String sanitizeFilename(String originalFilename) {
        String sanitized = originalFilename == null ? "file" : originalFilename.replaceAll("[^A-Za-z0-9._-]", "_");
        return sanitized.length() > 120 ? sanitized.substring(sanitized.length() - 120) : sanitized;
    }

    private String resolveExtension(String originalFilename) {
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
