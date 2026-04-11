package com.threlease.base.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 클라이언트 IP 추출 및 검증 유틸리티
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IpUtils {

    private static final List<String> IP_HEADER_CANDIDATES = Arrays.asList(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    );

    /**
     * 클라이언트의 실제 IP 주소를 추출합니다. (Proxy, LB, Cloudflare 대응)
     */
    public static String getClientIp(HttpServletRequest request) {
        return getClientIp(request, false);
    }

    public static String getClientIp(HttpServletRequest request, boolean trustForwardedHeaders) {
        if (!trustForwardedHeaders) {
            return request.getRemoteAddr();
        }
        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (StringUtils.hasText(ipList) && !"unknown".equalsIgnoreCase(ipList)) {
                // X-Forwarded-For 등에서 여러 IP가 올 경우 첫 번째 IP가 실제 클라이언트 IP
                return ipList.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 해당 IP가 사설 IP(Internal/Private) 대역인지 확인합니다.
     */
    public static boolean isInternalIp(String ip) {
        if (!StringUtils.hasText(ip)) return false;
        
        // IPv4 사설 대역: 10.x.x.x, 172.16.x.x ~ 172.31.x.x, 192.168.x.x, 127.0.0.1
        return ip.startsWith("10.") ||
               ip.startsWith("192.168.") ||
               ip.startsWith("127.0.0.1") ||
               ip.startsWith("localhost") ||
               (ip.startsWith("172.") && isWithinPrivateRange172(ip));
    }

    private static boolean isWithinPrivateRange172(String ip) {
        try {
            String[] parts = ip.split("\\.");
            int secondOctet = Integer.parseInt(parts[1]);
            return secondOctet >= 16 && secondOctet <= 31;
        } catch (Exception e) {
            return false;
        }
    }
}
