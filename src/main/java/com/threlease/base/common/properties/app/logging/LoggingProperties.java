package com.threlease.base.common.properties.app.logging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties("app.logging")
public class LoggingProperties {
    private List<String> excludeUrls = new ArrayList<>();
    private boolean request = true;
    private boolean response = false;
    private List<String> sensitiveFields = new ArrayList<>();
    private int maxPayloadSize = 1024; // Default to 1KB
}
