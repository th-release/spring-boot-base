package com.threlease.base.common.configs;

import com.threlease.base.common.properties.app.email.EmailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class EmailConfig {
    private final EmailProperties emailProperties;

    @Bean
    @ConditionalOnProperty(prefix = "app.email", name = "enabled", havingValue = "true")
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(emailProperties.getHost());
        sender.setPort(emailProperties.getPort());
        sender.setUsername(emailProperties.getUsername());
        sender.setPassword(emailProperties.getPassword());
        sender.setProtocol(emailProperties.getProtocol());

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", emailProperties.getProtocol());
        props.put("mail.smtp.auth", String.valueOf(emailProperties.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(emailProperties.isStarttls()));
        props.put("mail.smtp.connectiontimeout", String.valueOf(emailProperties.getConnectTimeoutMillis()));
        props.put("mail.smtp.timeout", String.valueOf(emailProperties.getReadTimeoutMillis()));
        props.put("mail.smtp.writetimeout", String.valueOf(emailProperties.getWriteTimeoutMillis()));
        return sender;
    }
}
