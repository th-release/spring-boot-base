package com.threlease.base.common.properties.storage.upload;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
@ConfigurationProperties("storage.upload")
@Getter
@Setter
public class UploadSecurityProperties {
    private boolean enabled = true;
    private DataSize maxFileSize = DataSize.ofMegabytes(10);
    private boolean blockDoubleExtension = true;
}
