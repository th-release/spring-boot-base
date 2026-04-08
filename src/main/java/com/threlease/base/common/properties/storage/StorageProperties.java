package com.threlease.base.common.properties.storage;

import com.threlease.base.common.properties.storage.cleanup.CleanupProperties;
import com.threlease.base.common.properties.storage.local.LocalProperties;
import com.threlease.base.common.properties.storage.upload.UploadSecurityProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("storage")
@Getter
@Setter
public class StorageProperties {
    private LocalProperties local;
    private CleanupProperties cleanup;
    private UploadSecurityProperties upload;
}
