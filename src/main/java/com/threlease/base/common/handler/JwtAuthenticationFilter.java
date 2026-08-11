package com.threlease.base.common.handler;

import com.threlease.base.common.HttpConstants;
import com.threlease.base.common.configs.ApiAccessRules;
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
    private final AuthService authService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return ApiAccessRules.isPublicRequest(request.getMethod(), request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authorization = request.getHeader(HttpConstants.HEADER_AUTHORIZATION);
            if (authorization == null || authorization.isBlank()) {
                throw new AuthenticationCredentialsNotFoundException("TOKEN_MISSING");
            }
            if (!authorization.toLowerCase().startsWith("bearer ")) {
                throw new BadCredentialsException("TOKEN_INVALID");
            }

            String token = authorization.substring(7).trim();
            if (token.isBlank()) {
                throw new BadCredentialsException("TOKEN_INVALID");
            }

            AuthEntity user = authService.findOneByToken(token)
                    .orElseThrow(() -> new BadCredentialsException("TOKEN_INVALID"));

            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(user.getUuid(), null, List.of());
            authentication.setDetails(user);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("user", user);

            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
