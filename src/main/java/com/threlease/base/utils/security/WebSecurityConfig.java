package com.threlease.base.utils.security;

import com.threlease.base.enums.Roles;
import com.threlease.base.utils.jsonwebtoken.JwtAuthenticationFilter;
import com.threlease.base.utils.jsonwebtoken.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtProvider jwtProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(management ->
                        management.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                )
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/community/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling((exceptionConfig) ->
                        exceptionConfig.authenticationEntryPoint(
                                ((request, response, authException) -> {
                                    BasicResponse b_response = BasicResponse.builder()
                                            .success(false)
                                            .message(Optional.of("인증되지 않은 사용자 입니다."))
                                            .data(Optional.empty())
                                            .build();
                                    response.setContentType("application/json");
                                    response.setStatus(401);
                                    response.getWriter().write(b_response.toJson());
                                })
                        ).accessDeniedHandler(((request, response, accessDeniedException) -> {
                            BasicResponse b_response = BasicResponse.builder()
                                    .success(false)
                                    .message(Optional.of("권한이 없는 사용자입니다."))
                                    .data(Optional.empty())
                                    .build();
                            response.setStatus(403);
                            response.setCharacterEncoding("utf-8");
                            response.setContentType("application/json");
                            response.getWriter().write(b_response.toJson());
                        }))
                );
        return http.build();
    }
}
