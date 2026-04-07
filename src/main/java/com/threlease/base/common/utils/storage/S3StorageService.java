package com.threlease.base.common.utils.storage;

import com.threlease.base.common.properties.aws.s3.S3Properties;
import com.threlease.base.common.utils.storage.entity.FileEntity;
import com.threlease.base.common.utils.storage.repository.FileRepository;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Template s3Template;
    private final FileRepository fileRepository;
    private final S3Properties s3Properties;

    @Override
    public FileEntity upload(MultipartFile file, String dirName) throws IOException {
        String bucket = s3Properties.getBucket();
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = dirName + "/" + fileName;

        s3Template.upload(bucket, filePath, file.getInputStream());

        FileEntity fileEntity = FileEntity.builder()
                .filePath(filePath)
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .dirName(dirName)
                .storageType(FileEntity.StorageType.S3)
                .url(getUrl(filePath))
                .build();

        return fileRepository.save(fileEntity);
    }

    @Override
    public void delete(String filePath) {
        String bucket = s3Properties.getBucket();
        // 실제 S3 파일 삭제
        try {
            s3Template.deleteObject(bucket, filePath);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: filePath={}", filePath, e);
        }

        // DB soft delete
        fileRepository.findByFilePathAndDeletedFalse(filePath)
                .ifPresentOrElse(
                        file -> {
                            file.markDeleted();
                            fileRepository.save(file);
                        },
                        () -> log.warn("S3 파일 삭제 요청: DB에서 해당 파일을 찾을 수 없습니다. filePath={}", filePath)
                );
    }

    @Override
    public String getUrl(String filePath) {
        String bucket = s3Properties.getBucket();
        return "https://" + bucket + ".s3.amazonaws.com/" + filePath;
    }
}
