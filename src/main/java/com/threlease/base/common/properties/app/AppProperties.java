package com.threlease.base.common.properties.app;

import com.threlease.base.common.properties.cors.CorsProperties;
import com.threlease.base.common.properties.app.jwt.JwtProperties;
import com.threlease.base.common.properties.app.redis.RedisProperties;
import com.threlease.base.common.properties.app.token.TokenProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app")
@Getter
@Setter
public class AppProperties {
    private RedisProperties redis;
    private TokenProperties token;
    private CorsProperties cors;
    private JwtProperties jwt;
}
