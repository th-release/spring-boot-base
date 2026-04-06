package com.threlease.base.common.utils.storage.repository;

import com.threlease.base.common.utils.storage.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    /** 경로로 활성 파일 조회 */
    Optional<FileEntity> findByFilePathAndDeletedFalse(String filePath);

    /** 특정 디렉토리의 활성 파일 전체 조회 */
    List<FileEntity> findAllByDirNameAndDeletedFalse(String dirName);
}
