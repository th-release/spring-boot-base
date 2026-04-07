package com.threlease.base.common.configs;

import com.threlease.base.common.ApiConstants;
import com.threlease.base.common.handler.ApiVersionHandlerMapping;
import com.threlease.base.common.interceptors.TokenInterceptor;
import com.threlease.base.common.properties.app.AppProperties;
import com.threlease.base.common.properties.cors.CorsProperties;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * MVC 관련으로 세팅하는 클래스
 */
@Configuration
@AllArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer, WebMvcRegistrations {
    private final TokenInterceptor tokenInterceptor;
    private final AppProperties appProperties;

    /**
     * API 버전 관리를 위한 커스텀 HandlerMapping 등록
     */
    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new ApiVersionHandlerMapping();
    }

    /**
     * API 경로 접두사 설정 (/api)
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(ApiConstants.API_PREFIX, c -> c.isAnnotationPresent(RestController.class));
    }

    /**
     * Interceptors 등록 함수
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String apiV1 = ApiConstants.API_PREFIX + "/" + ApiConstants.VERSION_1;
        
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns(apiV1 + "/**")
                .excludePathPatterns(
                        apiV1 + ApiConstants.AUTH_BASE + ApiConstants.AUTH_LOGIN,
                        apiV1 + ApiConstants.AUTH_BASE + ApiConstants.AUTH_SIGNUP,
                        apiV1 + ApiConstants.AUTH_BASE + ApiConstants.AUTH_REFRESH,
                        ApiConstants.API_PREFIX + ApiConstants.SWAGGER_UI,
                        ApiConstants.API_PREFIX + ApiConstants.API_DOCS,
                        ApiConstants.API_PREFIX + ApiConstants.ACTUATOR
                );
    }

    /**
     * Cors 관련 함수
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsProperties cors = appProperties.getCors();
        if (cors == null) return;
        
        registry.addMapping("/**") // 모든 경로에 CORS 설정 적용
                .allowedOriginPatterns(cors.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(cors.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders(cors.getAllowedHeaders().toArray(new String[0]))
                .exposedHeaders(cors.getExposedHeaders().toArray(new String[0]))
                .allowCredentials(cors.getAllowCredentials())
                .maxAge(cors.getMaxAge());
    }
}
