package com.threlease.base.common.properties.cors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("app.cors")
@Getter
@Setter
public class CorsProperties {
    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>();
    private List<String> allowedHeaders = new ArrayList<>();
    private List<String> exposedHeaders = new ArrayList<>();
    private Boolean allowCredentials = false;
    private Long maxAge = 1800L;
}
