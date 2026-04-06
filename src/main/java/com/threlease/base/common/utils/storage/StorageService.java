package com.threlease.base.common.utils.storage;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    /**
     * 파일을 저장하고 저장된 파일의 경로나 URL을 반환합니다.
     */
    String upload(MultipartFile file, String dirName) throws IOException;

    /**
     * 저장된 파일을 삭제합니다.
     */
    void delete(String filePath);

    /**
     * 저장된 파일의 풀 경로(또는 URL)를 반환합니다.
     */
    String getUrl(String filePath);
}
