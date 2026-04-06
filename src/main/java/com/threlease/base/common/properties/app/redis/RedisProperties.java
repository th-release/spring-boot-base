package com.threlease.base.common.properties.app.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.redis")
@Getter
@Setter
public class RedisProperties {
    private Boolean enabled;
    private String host;
    private String port;
}
