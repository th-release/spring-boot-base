package com.threlease.base.common.interceptors;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RestControllerGuardInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        if (request.getRequestURI().startsWith("/api/")) {
            return true;
        }

        if (AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RestController.class) != null) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return true;
    }
}
