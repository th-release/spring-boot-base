package com.threlease.base.common.properties.aws.region;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("spring.cloud.aws.region")
@Getter
@Setter
public class RegionProperties {
    private String Static;
}
