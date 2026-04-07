package com.threlease.base.common.utils.storage.batch;

import com.threlease.base.common.properties.aws.s3.S3Properties;
import com.threlease.base.common.properties.storage.StorageProperties;
import com.threlease.base.common.utils.storage.entity.FileEntity;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 스토리지 타입(LOCAL / S3)에 따라 실제 파일을 삭제하는 컴포넌트
 * OrphanFileCleanupJob 에서 주입받아 사용합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrphanFileDeleter {

    private final S3Template s3Template;
    private final S3Properties s3Properties;
    private final StorageProperties storageProperties;

    /**
     * FileEntity 의 스토리지 타입에 맞게 실제 파일을 삭제합니다.
     *
     * @return 삭제 성공 여부 (물리 파일이 이미 없어도 true 반환 — DB 정리는 진행)
     */
    public boolean deletePhysical(FileEntity file) {
        try {
            switch (file.getStorageType()) {
                case LOCAL -> deleteLocal(file.getFilePath());
                case S3    -> deleteS3(file.getFilePath());
                default    -> {
                    log.warn("[OrphanFileDeleter] 알 수 없는 스토리지 타입: {}, fileId={}",
                            file.getStorageType(), file.getId());
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            log.error("[OrphanFileDeleter] 파일 삭제 실패: fileId={}, path={}, type={}",
                    file.getId(), file.getFilePath(), file.getStorageType(), e);
            return false;
        }
    }

    private void deleteLocal(String filePath) throws IOException {
        String localRootPath = storageProperties.getLocal().getPath();
        var target = Paths.get(localRootPath, filePath);
        boolean deleted = Files.deleteIfExists(target);
        log.debug("[OrphanFileDeleter] 로컬 파일 {} : {}",
                deleted ? "삭제 완료" : "이미 없음 (DB만 정리)", target);
    }

    private void deleteS3(String filePath) {
        String bucket = s3Properties.getBucket();
        s3Template.deleteObject(bucket, filePath);
        log.debug("[OrphanFileDeleter] S3 파일 삭제 완료: bucket={}, key={}", bucket, filePath);
    }
}
