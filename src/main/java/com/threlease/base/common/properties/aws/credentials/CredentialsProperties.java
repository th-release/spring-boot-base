package com.threlease.base.common.properties.aws.credentials;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("spring.cloud.aws.credentials")
@Getter
@Setter
public class CredentialsProperties {
    private String accessKey;
    private String secretKey;
}
