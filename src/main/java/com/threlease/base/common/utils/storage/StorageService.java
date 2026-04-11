package com.threlease.base.common.utils.storage;

import com.threlease.base.common.utils.storage.entity.FileEntity;
import com.threlease.base.entities.AuthEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {

    /**
     * 파일을 저장하고 DB에 메타데이터를 기록한 뒤 FileEntity를 반환합니다.
     */
    FileEntity upload(MultipartFile file, String dirName, AuthEntity owner) throws IOException;

    /**
     * 저장된 파일을 삭제합니다 (실제 파일 삭제 + DB soft delete).
     */
    void delete(String filePath);

    /**
     * 저장된 파일의 풀 경로(또는 URL)를 반환합니다.
     */
    String getUrl(String filePath);

    /**
     * 다운로드용 URL을 생성합니다.
     */
    String getDownloadUrl(FileEntity fileEntity);
}
