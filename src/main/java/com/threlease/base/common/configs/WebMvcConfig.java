package com.threlease.base.common.configs;

import com.threlease.base.common.interceptors.TokenInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 관련으로 세팅하는 클래스
 */
@Configuration
@AllArgsConstructor
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    private final TokenInterceptor tokenInterceptor;

    /**
     * Interceptors 등록 함수
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/auth/publicKey", "/swagger", "/swagger-ui/**", "/v3/api-docs/**");
    }

    /**
     * Cors 관련 함수
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*") // OPTIONS 포함
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
