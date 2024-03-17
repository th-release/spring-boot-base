package com.threlease.base.utils.jsonwebtoken;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        try {
            if (jwtProvider.validateToken(token)) {
                Authentication auth = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException e) {
            throw new RuntimeException(e);
        }

        filterChain.doFilter(request, response);
    }
}
