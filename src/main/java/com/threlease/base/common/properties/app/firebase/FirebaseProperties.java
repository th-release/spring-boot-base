package com.threlease.base.common.properties.app.firebase;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.firebase")
@Getter
@Setter
public class FirebaseProperties {
    private boolean enabled = false;
    private String projectId;
    private String credentialsPath;
    private String credentialsJson;
    private String databaseUrl;

    @PostConstruct
    public void validate() {
        if (!enabled) {
            return;
        }

        if (isBlank(projectId)) {
            throw new IllegalStateException("app.firebase.enabled=true but app.firebase.project-id is blank");
        }
        if (isBlank(credentialsPath) && isBlank(credentialsJson)) {
            throw new IllegalStateException("app.firebase.enabled=true but neither app.firebase.credentials-path nor app.firebase.credentials-json is configured");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
