package com.threlease.base.common.properties.app.swagger.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.swagger")
@Getter
@Setter
public class SwaggerProperties {
    private String scheme;
    private String title;
    private String description;
    private String version;
}
