package com.threlease.base.common.handler;

import com.threlease.base.common.HttpConstants;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.utils.responses.BasicResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ErrorCode errorCode = authException instanceof AuthenticationCredentialsNotFoundException
                ? ErrorCode.TOKEN_MISSING
                : ErrorCode.TOKEN_INVALID;

        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(BasicResponse.error(
                errorCode.name(),
                errorCode.getMessage(),
                request.getRequestURI(),
                request.getHeader(HttpConstants.HEADER_CORRELATION_ID),
                null
        ).toJson());
    }
}
