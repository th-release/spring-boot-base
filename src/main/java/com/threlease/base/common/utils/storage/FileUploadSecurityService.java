package com.threlease.base.common.utils.storage;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.properties.storage.upload.UploadSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
}
