package com.threlease.base.common.properties.storage.cleanup;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CleanupProperties {
    private String cron = "0 0 3 * * *";
    private Long fixedRate = 0L;
    private Integer chunkSize = 100;
}
