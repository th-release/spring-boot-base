package com.threlease.base.common.properties.app.privacy;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.privacy")
@Getter
@Setter
public class PrivacyProperties {
    private boolean maskAuditIp = true;
    private boolean includeUserAgent = true;
    private int auditRetentionDays = 90;
}
