package com.threlease.base.common.configs;

import com.threlease.base.common.interceptors.TokenInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.*;

/**
 * MVC 관련으로 세팅하는 클래스
 */
@Configuration
@AllArgsConstructor
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    private final TokenInterceptor tokenInterceptor;

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
     * static 리소스 핸들러 등록
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 루트 및 정적 리소스 핸들링
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        // Swagger UI 전용 핸들러 (API prefix 영향 받지 않도록)
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
    }

    /**
     * SPA 라우팅 대응 (React Router)
     * API(/api/**)가 아닌 모든 요청을 index.html로 포워딩
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // API 경로(/api/**)를 제외한 모든 경로를 index.html로 리다이렉트 (SPA 지원)
        registry.addViewController("/{spring:\\w+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{spring:\\w+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{spring:\\w+}/**{spring:?!(\\.js|\\.css|\\.png|\\.jpg|\\.jpeg|\\.gif|\\.svg|\\.ico|\\.json)$}")
                .setViewName("forward:/index.html");
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
