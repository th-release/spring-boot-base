package com.threlease.base.common.utils.email;

import com.threlease.base.common.properties.app.email.EmailProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailProperties emailProperties;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public boolean isEnabled() {
        return emailProperties.isEnabled();
    }

    public void sendText(String to, String subject, String content) {
        send(to, subject, content, false);
    }

    public void sendHtml(String to, String subject, String content) {
        send(to, subject, content, true);
    }

    public void sendPasswordResetCode(String to, String code, int expireMinutes) {
        sendText(to, "[spring-boot-base] Password reset verification code",
                "Your password reset verification code is: " + code +
                        "\nThis code expires in " + Math.max(1, expireMinutes) + " minute(s).");
    }

    private void send(String to, String subject, String content, boolean html) {
        if (!emailProperties.isEnabled()) {
            throw new IllegalStateException("Email service is disabled");
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new IllegalStateException("Email service is enabled but JavaMailSender is not configured");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setFrom(emailProperties.getFromAddress(), emailProperties.getFromName());
            helper.setSubject(subject);
            helper.setText(content, html);
            mailSender.send(message);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }
}
