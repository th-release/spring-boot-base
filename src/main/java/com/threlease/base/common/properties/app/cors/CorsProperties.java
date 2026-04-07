package com.threlease.base.common.properties.app.cors;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CorsProperties {
    private List<String> allowedOrigins = List.of("*");
    private List<String> allowedMethods = List.of("*");
    private List<String> allowedHeaders = List.of("*");
    private List<String> exposedHeaders = List.of();
    private Boolean allowCredentials = true;
    private Long maxAge = 3600L;
}
