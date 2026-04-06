package com.threlease.base.common.properties.aws;

import com.threlease.base.common.properties.aws.credentials.CredentialsProperties;
import com.threlease.base.common.properties.aws.region.RegionProperties;
import com.threlease.base.common.properties.aws.s3.S3Properties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("spring.cloud.aws")
@Getter
@Setter
public class AwsProperties {
    private S3Properties s3;
    private RegionProperties region;
    private CredentialsProperties credentials;
}
