package com.threlease.base.common.utils.storage;

import com.threlease.base.common.properties.storage.StorageProperties;
import com.threlease.base.common.utils.storage.entity.FileEntity;
import com.threlease.base.common.utils.storage.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final FileRepository fileRepository;
    private final StorageProperties storageProperties;
    private final FileUploadSecurityService fileUploadSecurityService;

    @Override
    public FileEntity upload(MultipartFile file, String dirName) throws IOException {
        fileUploadSecurityService.validate(file);
        String rootPath = storageProperties.getLocal().getPath();
        String fileName = UUID.randomUUID() + "_" + fileUploadSecurityService.sanitizeFilename(file.getOriginalFilename());
        Path targetDir = Paths.get(rootPath, dirName);

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path targetPath = targetDir.resolve(fileName);
        file.transferTo(targetPath.toFile());

        String filePath = dirName + "/" + fileName;

        FileEntity fileEntity = FileEntity.builder()
                .filePath(filePath)
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .dirName(dirName)
                .storageType(FileEntity.StorageType.LOCAL)
                .url(getUrl(filePath))
                .build();

        return fileRepository.save(fileEntity);
    }

    @Override
    public void delete(String filePath) {
        String rootPath = storageProperties.getLocal().getPath();
        // 실제 파일 삭제
        try {
            Path fileToDelete = Paths.get(rootPath, filePath);
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            log.error("로컬 파일 삭제 실패: filePath={}", filePath, e);
        }

        // DB soft delete
        fileRepository.findByFilePathAndDeletedFalse(filePath)
                .ifPresentOrElse(
                        file -> {
                            file.markDeleted();
                            fileRepository.save(file);
                        },
                        () -> log.warn("로컬 파일 삭제 요청: DB에서 해당 파일을 찾을 수 없습니다. filePath={}", filePath)
                );
    }

    @Override
    public String getUrl(String filePath) {
        String prefix = storageProperties.getLocal().getPrefix();
        return prefix + "/" + filePath;
    }
}
