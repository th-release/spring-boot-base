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
    private List<String> sensitiveFields = new ArrayList<>(List.of(
            "password",
            "newPassword",
            "confirmPassword",
            "accessToken",
            "refreshToken",
            "token",
            "authorization",
            "refresh_token",
            "access_token",
            "idToken",
            "session",
            "sessionid",
            "sessiontoken",
            "clientSecret",
            "secret",
            "secretkey",
            "apiKey",
            "api_key",
            "privatekey",
            "credential",
            "credentials",
            "otp",
            "code",
            "authcode",
            "setcookie",
            "cookie"
    ));
    private List<String> sensitiveValuePatterns = new ArrayList<>(List.of(
            "(?i)Bearer\\s+[A-Za-z0-9\\-._~+/]+=*",
            "\\b[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\b",
            "(?i)(sk|rk)_[A-Za-z0-9]{16,}",
            "(?i)AKIA[0-9A-Z]{16}"
    ));
    private int maxPayloadSize = 1024; // Default to 1KB
}
