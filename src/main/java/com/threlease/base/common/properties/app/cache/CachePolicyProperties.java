package com.threlease.base.common.properties.app.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("app.cache")
@Getter
@Setter
public class CachePolicyProperties {
    private long defaultTtlSeconds = 3600;
    private List<String> names = new ArrayList<>(List.of("user"));
}
