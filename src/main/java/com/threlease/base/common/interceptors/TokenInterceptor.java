package com.threlease.base.common.interceptors;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
@AllArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("GET".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/api/v1/files/content/")) {
            return true;
        }

        String token = request.getHeader("Authorization");

        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCode.TOKEN_MISSING);
        }

        if (!token.toLowerCase().startsWith("bearer ")) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        Optional<AuthEntity> user = authService.findOneByToken(token);

        if (user.isEmpty()) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        authService.assertTokenUsable(user.get());

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(user.get().getUuid(), null, List.of());
        authentication.setDetails(user.get());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setAttribute("user", user.get());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        SecurityContextHolder.clearContext();
    }
}
