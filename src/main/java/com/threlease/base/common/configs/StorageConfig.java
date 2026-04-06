package com.threlease.base.common.configs;

import com.threlease.base.common.utils.storage.LocalStorageService;
import com.threlease.base.common.utils.storage.S3StorageService;
import com.threlease.base.common.utils.storage.StorageService;
import com.threlease.base.common.utils.storage.repository.FileRepository;
import io.awspring.cloud.s3.S3Template;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class StorageConfig {

    /**
     * AWS S3 설정(spring.cloud.aws.s3.bucket)이 있을 때 활성화되는 S3 Storage
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cloud.aws.s3.bucket")
    public StorageService s3StorageService(S3Template s3Template, FileRepository fileRepository) {
        return new S3StorageService(s3Template, fileRepository);
    }

    /**
     * AWS S3 설정이 없을 때 활성화되는 Local Storage (Fallback)
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.cloud.aws.s3.bucket", matchIfMissing = true, havingValue = "none")
    public StorageService localStorageService(FileRepository fileRepository) {
        return new LocalStorageService(fileRepository);
    }
}
