package com.threlease.base.common.utils.storage;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String upload(MultipartFile file, String dirName) throws IOException {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        
        s3Template.upload(bucket, fileName, file.getInputStream());
        
        return fileName;
    }

    @Override
    public void delete(String filePath) {
        s3Template.deleteObject(bucket, filePath);
    }

    @Override
    public String getUrl(String filePath) {
        // S3 공개 설정인 경우 URL을 반환, 비공개인 경우 별도 로직 필요
        return "https://" + bucket + ".s3.amazonaws.com/" + filePath;
    }
}
