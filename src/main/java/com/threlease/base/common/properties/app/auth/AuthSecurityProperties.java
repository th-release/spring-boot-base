package com.threlease.base.common.properties.app.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("app.auth")
@Getter
@Setter
public class AuthSecurityProperties {
    private Audit audit = new Audit();
    private LoginFailure loginFailure = new LoginFailure();
    private Mfa mfa = new Mfa();
    private PasswordReset passwordReset = new PasswordReset();

    @Getter
    @Setter
    public static class Audit {
        private boolean enabled = true;
        private boolean includeUserAgent = true;
    }

    @Getter
    @Setter
    public static class LoginFailure {
        private boolean enabled = true;
        private int maxAttempts = 5;
        private long lockMinutes = 15;
    }

    @Getter
    @Setter
    public static class Mfa {
        private boolean enabled = false;
        private String issuer = "spring-boot-base";
        private int codeDigits = 6;
        private int timeStepSeconds = 30;
        private int allowedWindows = 1;
        private List<String> requiredTypes = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class PasswordReset {
        private int codeExpireMinutes = 10;
        private int maxVerificationAttempts = 5;
        private long verificationLockMinutes = 15;
    }
}
