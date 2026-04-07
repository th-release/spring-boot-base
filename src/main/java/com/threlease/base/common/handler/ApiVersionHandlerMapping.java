package com.threlease.base.common.handler;

import com.threlease.base.common.annotation.ApiVersion;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * @ApiVersion 어노테이션을 처리하기 위한 커스텀 RequestMappingHandlerMapping
 * URL 패턴 앞에 /v1, /v2 등의 버전을 자동으로 추가합니다.
 */
public class ApiVersionHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
        if (info == null) return null;

        ApiVersion methodAnnotation = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        if (methodAnnotation != null) {
            return createApiVersionInfo(methodAnnotation, info);
        }

        ApiVersion typeAnnotation = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
        if (typeAnnotation != null) {
            return createApiVersionInfo(typeAnnotation, info);
        }

        return info;
    }

    private RequestMappingInfo createApiVersionInfo(ApiVersion annotation, RequestMappingInfo info) {
        int[] versions = annotation.value();
        String[] prefixes = new String[versions.length];
        for (int i = 0; i < versions.length; i++) {
            prefixes[i] = "v" + versions[i];
        }

        RequestMappingInfo.Builder builder = info.mutate();
        // 각 패턴 앞에 버전 접두사 추가
        builder.paths(prefixes);
        
        return builder.build().combine(info);
    }

    @Override
    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        ApiVersion apiVersion = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
        return createCondition(apiVersion);
    }

    @Override
    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        ApiVersion apiVersion = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        return createCondition(apiVersion);
    }

    private RequestCondition<?> createCondition(ApiVersion apiVersion) {
        return apiVersion == null ? null : new ApiVersionRequestCondition(apiVersion.value());
    }
}
