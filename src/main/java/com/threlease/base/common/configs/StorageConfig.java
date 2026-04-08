package com.threlease.base.common.configs;

import com.threlease.base.common.properties.aws.AwsProperties;
import com.threlease.base.common.properties.storage.upload.UploadSecurityProperties;
import com.threlease.base.common.utils.storage.LocalStorageService;
import com.threlease.base.common.utils.storage.S3StorageService;
import com.threlease.base.common.utils.storage.StorageService;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class StorageConfig {

    /**
     * S3StorageService 빈이 존재할 때(= awspring S3 의존성이 있을 때) S3를 기본 StorageService로 등록
     */
    @Bean
    @Primary
    @ConditionalOnBean(S3StorageService.class)
    public StorageService s3StorageServiceBean(S3StorageService s3StorageService) {
        return s3StorageService;
    }

    /**
     * S3StorageService 빈이 없을 때 LocalStorageService를 기본 StorageService로 등록 (Fallback)
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(S3StorageService.class)
    public StorageService localStorageServiceBean(LocalStorageService localStorageService) {
        return localStorageService;
    }

    /**
     * 업로드 한도는 storage.upload.max-file-size 하나만 기준으로 삼고
     * Spring multipart 설정도 동일한 값으로 통일한다.
     */
    @Bean
    public MultipartConfigElement multipartConfigElement(UploadSecurityProperties uploadSecurityProperties) {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        DataSize maxSize = uploadSecurityProperties.getMaxFileSize();
        factory.setMaxFileSize(maxSize);
        factory.setMaxRequestSize(maxSize);
        return factory.createMultipartConfig();
    }

    @Bean
    @ConditionalOnBean(S3Client.class)
    public S3Presigner s3Presigner(AwsProperties awsProperties) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(awsProperties.getRegion().getStatic()));

        String accessKey = awsProperties.getCredentials().getAccessKey();
        String secretKey = awsProperties.getCredentials().getSecretKey();
        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }
}
