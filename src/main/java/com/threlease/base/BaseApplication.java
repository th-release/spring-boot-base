package com.threlease.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.security.SecureRandom;
import java.util.Base64;


@EnableCaching
@EnableScheduling
@EnableJpaRepositories
@SpringBootApplication
public class BaseApplication {
	public static String generateBase64Key() {
		byte[] key = new byte[32]; // 256 bit
		new SecureRandom().nextBytes(key);
		return Base64.getEncoder().encodeToString(key);
	}

	public static void main(String[] args) {
		System.out.println("=== AES-256 Secret Key ===");
		System.out.println(generateBase64Key());
		SpringApplication.run(BaseApplication.class, args);
	}
}
