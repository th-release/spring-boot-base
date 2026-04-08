package com.threlease.base.common.utils.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.threlease.base.common.properties.app.firebase.FirebaseProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FirebaseUtils {
    private final FirebaseProperties firebaseProperties;
    private final ObjectProvider<FirebaseApp> firebaseAppProvider;

    public boolean isEnabled() {
        return firebaseProperties.isEnabled();
    }

    public String sendNotification(String targetToken, String title, String body, Map<String, String> data) throws Exception {
        FirebaseApp app = getFirebaseApp();
        Message.Builder builder = Message.builder()
                .setToken(targetToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());
        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }
        return FirebaseMessaging.getInstance(app).send(builder.build());
    }

    public FirebaseToken verifyIdToken(String idToken) throws Exception {
        return FirebaseAuth.getInstance(getFirebaseApp()).verifyIdToken(idToken);
    }

    private FirebaseApp getFirebaseApp() {
        if (!firebaseProperties.isEnabled()) {
            throw new IllegalStateException("Firebase is disabled");
        }
        FirebaseApp firebaseApp = firebaseAppProvider.getIfAvailable();
        if (firebaseApp == null) {
            throw new IllegalStateException("Firebase is enabled but FirebaseApp is not configured");
        }
        return firebaseApp;
    }
}
