package com.threlease.base.common.properties.storage.local;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("storage.local")
@Getter
@Setter
public class LocalProperties {
    private String path;
    private String prefix;
}
