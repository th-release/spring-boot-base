package com.threlease.base.common.handler;

import com.threlease.base.common.utils.XssUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servlet 요청의 파라미터를 XSS 방지 처리하는 래퍼 클래스
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        return XssUtils.escape(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;

        String[] escapedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            escapedValues[i] = XssUtils.escape(values[i]);
        }
        return escapedValues;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameterMap = super.getParameterMap();
        Map<String, String[]> escapedMap = new LinkedHashMap<>();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            escapedMap.put(entry.getKey(), getParameterValues(entry.getKey()));
        }

        return escapedMap;
    }

    @Override
    public String getQueryString() {
        return XssUtils.escape(super.getQueryString());
    }
}
