package com.threlease.base.common.handler;

import com.threlease.base.common.utils.XssUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

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

        for (int i = 0; i < values.length; i++) {
            values[i] = XssUtils.escape(values[i]);
        }
        return values;
    }

    @Override
    public String getHeader(String name) {
        return XssUtils.escape(super.getHeader(name));
    }
}
