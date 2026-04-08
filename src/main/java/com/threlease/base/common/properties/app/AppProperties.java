package com.threlease.base.common.properties.app;

import com.threlease.base.common.properties.app.auth.AuthSecurityProperties;
import com.threlease.base.common.properties.app.cache.CachePolicyProperties;
import com.threlease.base.common.properties.app.database.DatabaseProperties;
import com.threlease.base.common.properties.app.email.EmailProperties;
import com.threlease.base.common.properties.app.firebase.FirebaseProperties;
import com.threlease.base.common.properties.cors.CorsProperties;
import com.threlease.base.common.properties.app.jwt.JwtProperties;
import com.threlease.base.common.properties.app.outbound.OutboundProperties;
import com.threlease.base.common.properties.app.privacy.PrivacyProperties;
import com.threlease.base.common.properties.app.redis.RedisProperties;
import com.threlease.base.common.properties.app.security.SecurityProperties;
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
    private AuthSecurityProperties auth;
    private OutboundProperties outbound;
    private SecurityProperties security;
    private CachePolicyProperties cache;
    private PrivacyProperties privacy;
    private DatabaseProperties database;
    private EmailProperties email;
    private FirebaseProperties firebase;
}
