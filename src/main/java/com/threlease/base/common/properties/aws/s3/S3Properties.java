package com.threlease.base.common.properties.aws.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("spring.cloud.aws.s3")
@Getter
@Setter
public class S3Properties {
    private String bucket;
}
