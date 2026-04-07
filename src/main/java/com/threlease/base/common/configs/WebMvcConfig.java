package com.threlease.base.common.configs;

import com.threlease.base.common.interceptors.TokenInterceptor;
import com.threlease.base.common.properties.app.AppProperties;
import com.threlease.base.common.properties.app.cors.CorsProperties;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.*;

/**
 * MVC 관련으로 세팅하는 클래스
 */
@Configuration
@AllArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final TokenInterceptor tokenInterceptor;
    private final AppProperties appProperties;

    /**
     * API 경로 접두사 설정 (/api)
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api", c -> c.isAnnotationPresent(RestController.class));
    }

    /**
     * Interceptors 등록 함수
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/api/**") // API 경로만 인터셉트
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/publicKey",
                        "/api/auth/signup",
                        "/api/swagger",
                        "/api/swagger-ui/**",
                        "/api/v3/api-docs/**",
                        "/api/actuator/**"
                );
    }

    /**
     * Cors 관련 함수
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsProperties cors = appProperties.getCors();
        if (cors == null) {
            cors = new CorsProperties();
        }

        registry.addMapping("/**")
                .allowedOriginPatterns(cors.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(cors.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders(cors.getAllowedHeaders().toArray(new String[0]))
                .exposedHeaders(cors.getExposedHeaders().toArray(new String[0]))
                .allowCredentials(cors.getAllowCredentials())
                .maxAge(cors.getMaxAge());
    }
}
