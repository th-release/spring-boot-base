package com.threlease.base.common.properties.storage.upload;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("storage.upload")
@Getter
@Setter
public class UploadSecurityProperties {
    private boolean enabled = true;
    private long maxFileSizeBytes = 10485760L;
    private boolean blockDoubleExtension = true;
    private List<String> allowedExtensions = new ArrayList<>(List.of("jpg", "jpeg", "png", "gif", "pdf", "txt", "csv"));
    private List<String> allowedContentTypes = new ArrayList<>(List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf",
            "text/plain",
            "text/csv"
    ));
}
