package com.threlease.base.common.handler;

import com.threlease.base.common.HttpConstants;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String[] PUBLIC_PATH_PREFIXES = {
            "/api/v1/auth/login",
            "/api/v1/auth/signup",
            "/api/v1/auth/refresh",
            "/api/v1/auth/password/reset/request",
            "/api/v1/auth/password/reset/confirm",
            "/api/v1/files/content/",
            "/api/common/enums",
            "/api/swagger-ui/",
            "/api/v3/api-docs/",
            "/api/actuator/health"
    };

    private final AuthService authService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/")) {
            return true;
        }
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authorization = request.getHeader(HttpConstants.HEADER_AUTHORIZATION);
            if (authorization == null || authorization.isBlank()) {
                throw new AuthenticationCredentialsNotFoundException("TOKEN_MISSING");
            }

            AuthEntity user = authService.findOneByToken(authorization)
                    .orElseThrow(() -> new BadCredentialsException("TOKEN_INVALID"));

            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(user.getUuid(), null, List.of());
            authentication.setDetails(user);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
