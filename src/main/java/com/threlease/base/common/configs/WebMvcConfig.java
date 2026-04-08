package com.threlease.base.common.configs;

import com.threlease.base.common.annotation.ApiVersion;
import com.threlease.base.common.interceptors.ApiVersionInterceptor;
import com.threlease.base.common.interceptors.TokenInterceptor;
import com.threlease.base.common.properties.app.AppProperties;
import com.threlease.base.common.properties.cors.CorsProperties;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.*;

/**
 * MVC 관련 설정 클래스
 */
@Configuration
@AllArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final TokenInterceptor tokenInterceptor;
    private final ApiVersionInterceptor apiVersionInterceptor;
    private final AppProperties appProperties;

    /**
     * @ApiVersion(n) 어노테이션 기반 자동 URL 접두사(/api/vn) 설정
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 명시적으로 "/api" 사용
        String apiPrefix = "/api";

        // 1. 버전이 명시된 API들 처리 (v1 ~ v100)
        for (int i = 1; i <= 100; i++) {
            final int version = i;
            configurer.addPathPrefix(apiPrefix + "/v" + version, 
                c -> {
                    ApiVersion ann = AnnotationUtils.findAnnotation(c, ApiVersion.class);
                    return ann != null && ann.value() == version;
                }
            );
        }

        // 2. 버전이 없는 일반 @RestController들 처리
        configurer.addPathPrefix(apiPrefix, 
            c -> c.isAnnotationPresent(RestController.class) && AnnotationUtils.findAnnotation(c, ApiVersion.class) == null);
    }

    /**
     * 인터셉터 및 공개 경로(Public Path) 설정
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String apiV1 = "/api/v1";

        registry.addInterceptor(apiVersionInterceptor)
                .addPathPatterns("/api/**");

        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns(apiV1 + "/**")
                .excludePathPatterns(
                        // 모든 경로를 리터럴로 명시하여 직관성 확보
                        apiV1 + "/auth/login",
                        apiV1 + "/auth/signup",
                        apiV1 + "/auth/refresh",
                        "/api/swagger-ui/**",
                        "/api/v3/api-docs/**",
                        "/api/actuator/**"
                );
    }

    /**
     * CORS 설정
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsProperties cors = appProperties.getCors();
        if (cors == null) return;
        
        registry.addMapping("/**")
                .allowedOriginPatterns(cors.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(cors.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders(cors.getAllowedHeaders().toArray(new String[0]))
                .exposedHeaders(cors.getExposedHeaders().toArray(new String[0]))
                .allowCredentials(cors.getAllowCredentials())
                .maxAge(cors.getMaxAge());
    }
}
