package com.threlease.base.common.properties.storage.cleanup;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("storage.cleanup")
@Getter
@Setter
public class CleanupProperties {
    private String cron;
    private Long fixedRate;
    private Integer chunkSize;
}
