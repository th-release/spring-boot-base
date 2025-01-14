package com.threlease.base.common.interceptors;

import com.threlease.base.common.exception.TokenValidException;
import com.threlease.base.entites.AuthEntity;
import com.threlease.base.functions.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * 토큰 검증 Interceptor
 */
@Component
@AllArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Authorization 헤더 값 가져오기
        String authorizationHeader = request.getHeader("Authorization");

        // 헤더가 없는 경우 처리
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            throw new TokenValidException("400: Missing Authorization Header");
        }

        // 추가적으로 토큰 검증 로직이 필요한 경우 구현 (예: JWT 토큰 검증)
        if (!isValidToken(authorizationHeader)) {
            throw new TokenValidException("403:Invalid Token");
        }

        Optional<AuthEntity> user = authService.findOneByToken(authorizationHeader);

        if (user.isEmpty()) {
            throw new TokenValidException("404:User Not Found");
        }

        request.setAttribute("user", user.get());

        return true; // 다음 단계로 요청 진행
    }

    private boolean isValidToken(String token) {
        // 여기에 토큰 검증 로직 작성 (JWT 파싱, 서명 확인 등)
        return token.toLowerCase().startsWith("bearer ");
    }
}

