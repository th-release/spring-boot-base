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
@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
		org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class,
		org.redisson.spring.starter.RedissonAutoConfigurationV2.class
})
public class BaseApplication {
	public static void main(String[] args) {
		SpringApplication.run(BaseApplication.class, args);
	}
}
