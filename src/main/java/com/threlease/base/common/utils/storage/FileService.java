package com.threlease.base.common.utils.storage;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.properties.storage.StorageProperties;
import com.threlease.base.common.utils.storage.dto.FileDownloadUrlDto;
import com.threlease.base.common.utils.storage.dto.FileUploadResponseDto;
import com.threlease.base.common.utils.storage.entity.FileEntity;
import com.threlease.base.common.utils.storage.repository.FileRepository;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.AuthPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final StorageService storageService;
    private final StorageProperties storageProperties;
    private final FileDownloadTokenService fileDownloadTokenService;
    private final AuthPermissionService authPermissionService;

    @Transactional
    public FileUploadResponseDto upload(MultipartFile file, String dirName, AuthEntity user) throws IOException {
        FileEntity fileEntity = storageService.upload(file, dirName, user);
        return FileUploadResponseDto.builder()
                .id(fileEntity.getId())
                .originalFileName(fileEntity.getOriginalFileName())
                .filePath(fileEntity.getFilePath())
                .url(resolveImmediateAccessUrl(fileEntity))
                .contentType(fileEntity.getContentType())
                .fileSize(fileEntity.getFileSize())
                .dirName(fileEntity.getDirName())
                .storageType(fileEntity.getStorageType().name())
                .build();
    }

    @Transactional
    public void delete(Long id, AuthEntity user) {
        FileEntity fileEntity = findOwnedFile(id, user);
        storageService.delete(fileEntity.getFilePath());
    }

    @Transactional(readOnly = true)
    public FileDownloadUrlDto createDownloadUrl(Long id, AuthEntity user) {
        FileEntity fileEntity = findOwnedFile(id, user);

        return FileDownloadUrlDto.builder()
                .id(fileEntity.getId())
                .fileName(fileEntity.getOriginalFileName())
                .storageType(fileEntity.getStorageType().name())
                .downloadUrl(resolveDownloadUrl(fileEntity))
                .expiresAt(fileEntity.getStorageType() == FileEntity.StorageType.S3 ? LocalDateTime.now().plusMinutes(10) : null)
                .build();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> serve(String filePath, String token, boolean download) {
        FileEntity fileEntity = fileRepository.findByFilePathAndDeletedFalse(filePath)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        if (fileEntity.getStorageType() == FileEntity.StorageType.S3) {
            return ResponseEntity.status(302)
                    .location(URI.create(fileEntity.getUrl()))
                    .build();
        }

        validateLocalDownloadToken(fileEntity, token);
        return buildLocalFileResponse(fileEntity, download);
    }

    private ResponseEntity<Resource> buildLocalFileResponse(FileEntity fileEntity, boolean download) {
        try {
            Path file = Paths.get(storageProperties.getLocal().getPath(), fileEntity.getFilePath());
            if (!Files.exists(file) || !Files.isReadable(file)) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
            }

            Resource resource = new UrlResource(file.toUri());
            String contentType = fileEntity.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = Files.probeContentType(file);
            }
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                            .filename(fileEntity.getOriginalFileName())
                            .build()
                            .toString())
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "파일 경로를 해석할 수 없습니다.");
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "파일을 읽는 중 오류가 발생했습니다.");
        }
    }

    private FileEntity findOwnedFile(Long id, AuthEntity user) {
        if (authPermissionService.hasPermission(user, AuthPermissionService.SYSTEM_ADMIN)) {
            return fileRepository.findActiveById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
        }

        return fileRepository.findActiveByIdAndOwner(id, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
    }

    private String resolveImmediateAccessUrl(FileEntity fileEntity) {
        if (fileEntity.getStorageType() == FileEntity.StorageType.S3) {
            return fileEntity.getUrl();
        }
        String token = fileDownloadTokenService.createToken(fileEntity.getId(), fileEntity.getFilePath(), 10);
        return fileEntity.getUrl() + "?token=" + token;
    }

    private String resolveDownloadUrl(FileEntity fileEntity) {
        if (fileEntity.getStorageType() == FileEntity.StorageType.S3) {
            return storageService.getDownloadUrl(fileEntity);
        }

        String token = fileDownloadTokenService.createToken(fileEntity.getId(), fileEntity.getFilePath(), 10);
        return fileEntity.getUrl() + "?token=" + token + "&download=true";
    }

    private void validateLocalDownloadToken(FileEntity fileEntity, String token) {
        FileDownloadTokenService.FileDownloadClaims claims = fileDownloadTokenService.verify(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        if (!fileEntity.getId().equals(claims.fileId()) || !fileEntity.getFilePath().equals(claims.filePath())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
