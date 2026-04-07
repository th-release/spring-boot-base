package com.threlease.base.common.configs;

import com.threlease.base.common.utils.storage.LocalStorageService;
import com.threlease.base.common.utils.storage.S3StorageService;
import com.threlease.base.common.utils.storage.StorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    /**
     * S3StorageService 빈이 존재할 때(= awspring S3 의존성이 있을 때) S3를 기본 StorageService로 등록
     */
    @Bean
    @ConditionalOnBean(S3StorageService.class)
    public StorageService s3StorageServiceBean(S3StorageService s3StorageService) {
        return s3StorageService;
    }

    /**
     * S3StorageService 빈이 없을 때 LocalStorageService를 기본 StorageService로 등록 (Fallback)
     */
    @Bean
    @ConditionalOnMissingBean(S3StorageService.class)
    public StorageService localStorageServiceBean(LocalStorageService localStorageService) {
        return localStorageService;
    }
}
