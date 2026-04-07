package com.threlease.base.common.handler;

import com.threlease.base.common.annotation.AllowHtml;
import com.threlease.base.common.utils.XssUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

@ControllerAdvice
public class XssRequestBodyAdvice extends RequestBodyAdviceAdapter {
    private static final Set<String> SKIP_FIELD_NAMES = Set.of(
            "password",
            "newPassword",
            "confirmPassword",
            "accessToken",
            "refreshToken",
            "token",
            "authorization",
            "secretKey"
    );

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        sanitizeObject(body, new IdentityHashMap<>());
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    private void sanitizeObject(Object target, IdentityHashMap<Object, Boolean> visited) {
        if (target == null || isSimpleValueType(target.getClass()) || visited.containsKey(target)) {
            return;
        }

        visited.put(target, Boolean.TRUE);

        if (target instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String stringValue) {
                    String keyName = entry.getKey() == null ? null : entry.getKey().toString();
                    if (!shouldSkipField(keyName)) {
                        ((Map<Object, Object>) map).put(entry.getKey(), XssUtils.sanitize(stringValue));
                    }
                } else {
                    sanitizeObject(value, visited);
                }
            }
            return;
        }

        if (target instanceof Collection<?> collection) {
            for (Object item : collection) {
                sanitizeObject(item, visited);
            }
            return;
        }

        if (target.getClass().isArray()) {
            int length = Array.getLength(target);
            for (int i = 0; i < length; i++) {
                Object item = Array.get(target, i);
                if (item instanceof String stringValue) {
                    Array.set(target, i, XssUtils.sanitize(stringValue));
                } else {
                    sanitizeObject(item, visited);
                }
            }
            return;
        }

        for (Field field : target.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object value = field.get(target);
                if (value instanceof String stringValue) {
                    AllowHtml allowHtml = field.getAnnotation(AllowHtml.class);
                    if (allowHtml != null) {
                        field.set(target, XssUtils.sanitize(stringValue, allowHtml.policy()));
                    } else if (!shouldSkipField(field.getName())) {
                        field.set(target, XssUtils.sanitize(stringValue));
                    }
                } else {
                    sanitizeObject(value, visited);
                }
            } catch (IllegalAccessException ignored) {
                // 접근할 수 없는 필드는 그대로 둡니다.
            }
        }
    }

    private boolean shouldSkipField(String fieldName) {
        String normalized = normalizeFieldName(fieldName);
        return SKIP_FIELD_NAMES.stream()
                .map(this::normalizeFieldName)
                .anyMatch(normalized::contains);
    }

    private String normalizeFieldName(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    }

    private boolean isSimpleValueType(Class<?> type) {
        return type.isPrimitive()
                || String.class.equals(type)
                || Number.class.isAssignableFrom(type)
                || Boolean.class.equals(type)
                || Character.class.equals(type)
                || type.isEnum()
                || Temporal.class.isAssignableFrom(type);
    }
}
