package com.threlease.base.common.interceptors;

import com.threlease.base.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class RestControllerGuardInterceptorTest {
    private final RestControllerGuardInterceptor interceptor = new RestControllerGuardInterceptor();

    @Test
    void blocksRestControllerOutsideApiPrefix() throws Exception {
        HandlerMethod handlerMethod = handlerMethod(DummyRestController.class, "sample");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        org.mockito.Mockito.when(request.getRequestURI()).thenReturn("/sample");

        assertThrows(BusinessException.class, () -> interceptor.preHandle(request, response, handlerMethod));
    }

    @Test
    void allowsRestControllerUnderApiPrefix() throws Exception {
        HandlerMethod handlerMethod = handlerMethod(DummyRestController.class, "sample");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        org.mockito.Mockito.when(request.getRequestURI()).thenReturn("/api/v1/sample");

        assertDoesNotThrow(() -> interceptor.preHandle(request, response, handlerMethod));
    }

    private HandlerMethod handlerMethod(Class<?> type, String methodName) throws Exception {
        Method method = type.getDeclaredMethod(methodName);
        return new HandlerMethod(type.getDeclaredConstructor().newInstance(), method);
    }

    @RestController
    @Component
    static class DummyRestController {
        @GetMapping("/sample")
        public String sample() {
            return "ok";
        }
    }
}
