package com.threlease.base.common.utils.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
public class LocalStorageService implements StorageService {

    @Value("${storage.local.path:./uploads}")
    private String rootPath;

    @Value("${storage.local.url-prefix:/api/files}")
    private String urlPrefix;

    @Override
    public String upload(MultipartFile file, String dirName) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetDir = Paths.get(rootPath, dirName);
        
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path targetPath = targetDir.resolve(fileName);
        file.transferTo(targetPath.toFile());

        return dirName + "/" + fileName;
    }

    @Override
    public void delete(String filePath) {
        try {
            Path fileToDelete = Paths.get(rootPath, filePath);
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            log.error("Failed to delete local file: {}", filePath, e);
        }
    }

    @Override
    public String getUrl(String filePath) {
        return urlPrefix + "/" + filePath;
    }
}
