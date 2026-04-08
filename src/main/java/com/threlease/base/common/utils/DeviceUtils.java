package com.threlease.base.common.utils;

public final class DeviceUtils {
    private DeviceUtils() {
    }

    public static String describe(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown device";
        }

        String browser = resolveBrowser(userAgent);
        String os = resolveOs(userAgent);
        return browser + " on " + os;
    }

    private static String resolveBrowser(String userAgent) {
        String value = userAgent.toLowerCase();
        if (value.contains("edg/")) return "Edge";
        if (value.contains("chrome/")) return "Chrome";
        if (value.contains("safari/") && !value.contains("chrome/")) return "Safari";
        if (value.contains("firefox/")) return "Firefox";
        if (value.contains("msie") || value.contains("trident/")) return "Internet Explorer";
        return "Unknown browser";
    }

    private static String resolveOs(String userAgent) {
        String value = userAgent.toLowerCase();
        if (value.contains("windows")) return "Windows";
        if (value.contains("mac os")) return "macOS";
        if (value.contains("iphone") || value.contains("ipad")) return "iOS";
        if (value.contains("android")) return "Android";
        if (value.contains("linux")) return "Linux";
        return "Unknown OS";
    }
}
