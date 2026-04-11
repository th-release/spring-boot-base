package com.threlease.base.common.utils;

import com.threlease.base.common.properties.app.security.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientIpResolver {
    private final SecurityProperties securityProperties;

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return IpUtils.getClientIp(request, securityProperties.getTrustedProxy().isForwardedHeadersEnabled());
    }
}
