package com.threlease.base.common.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.threlease.base.common.properties.app.firebase.FirebaseProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {
    private static final String FIREBASE_APP_NAME = "spring-boot-base";

    private final FirebaseProperties firebaseProperties;

    @Bean
    @ConditionalOnProperty(prefix = "app.firebase", name = "enabled", havingValue = "true")
    public FirebaseApp firebaseApp() throws Exception {
        for (FirebaseApp app : FirebaseApp.getApps()) {
            if (FIREBASE_APP_NAME.equals(app.getName())) {
                return app;
            }
        }

        FirebaseOptions.Builder builder = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(openCredentialsStream()))
                .setProjectId(firebaseProperties.getProjectId());

        if (firebaseProperties.getDatabaseUrl() != null && !firebaseProperties.getDatabaseUrl().isBlank()) {
            builder.setDatabaseUrl(firebaseProperties.getDatabaseUrl());
        }

        return FirebaseApp.initializeApp(builder.build(), FIREBASE_APP_NAME);
    }

    private InputStream openCredentialsStream() throws Exception {
        if (firebaseProperties.getCredentialsPath() != null && !firebaseProperties.getCredentialsPath().isBlank()) {
            return new FileInputStream(firebaseProperties.getCredentialsPath());
        }

        String credentialsJson = firebaseProperties.getCredentialsJson();
        if (credentialsJson == null || credentialsJson.isBlank()) {
            throw new IllegalStateException("Firebase credentials are missing");
        }

        String normalized = credentialsJson.trim();
        if (!normalized.startsWith("{")) {
            try {
                normalized = new String(Base64.getDecoder().decode(normalized), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException ignored) {
                // treat as raw json if Base64 decode is not possible
            }
        }
        return new ByteArrayInputStream(normalized.getBytes(StandardCharsets.UTF_8));
    }
}
