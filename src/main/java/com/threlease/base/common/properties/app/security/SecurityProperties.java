package com.threlease.base.common.properties.app.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.security")
@Getter
@Setter
public class SecurityProperties {
    private Headers headers = new Headers();

    @Getter
    @Setter
    public static class Headers {
        private boolean hstsEnabled = true;
        private String contentSecurityPolicy = "default-src 'self'; script-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'";
        private String referrerPolicy = "strict-origin-when-cross-origin";
    }
}
