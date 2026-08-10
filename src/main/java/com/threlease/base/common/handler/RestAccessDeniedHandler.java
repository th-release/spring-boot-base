package com.threlease.base.common.handler;

import com.threlease.base.common.HttpConstants;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.utils.responses.BasicResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(ErrorCode.FORBIDDEN.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(BasicResponse.error(
                ErrorCode.FORBIDDEN.name(),
                ErrorCode.FORBIDDEN.getMessage(),
                request.getRequestURI(),
                request.getHeader(HttpConstants.HEADER_CORRELATION_ID),
                null
        ).toJson());
    }
}
