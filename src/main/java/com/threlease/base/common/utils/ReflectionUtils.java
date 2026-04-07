package com.threlease.base.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 리플렉션 관련 유틸리티 메서드를 제공하는 정적 클래스입니다.
 * 경고: 리플렉션은 오용될 경우 런타임 오류나 보안 취약점을 야기할 수 있으므로 주의하여 사용해야 합니다.
 */
public class ReflectionUtils {

    private ReflectionUtils() {
        // 정적 유틸리티 클래스는 인스턴스화할 수 없습니다.
    }

    /**
     * 클래스에서 특정 이름의 필드를 가져옵니다. 상위 클래스의 필드도 탐색합니다.
     *
     * @param clazz 필드를 찾을 클래스
     * @param fieldName 찾을 필드의 이름
     * @return 찾은 Field 객체, 없으면 Optional.empty()
     */
    public static Optional<Field> getField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                Field field = currentClass.getDeclaredField(fieldName);
                field.setAccessible(true); // private 필드 접근 허용
                return Optional.of(field);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass(); // 상위 클래스에서 찾기
            }
        }
        return Optional.empty();
    }

    /**
     * 객체에서 필드 값을 가져옵니다.
     *
     * @param obj 필드 값을 가져올 객체
     * @param field 필드 객체
     * @param <T> 필드 값의 타입
     * @return 필드 값, 가져올 수 없으면 Optional.empty()
     */
    public static <T> Optional<T> getFieldValue(Object obj, Field field) {
        try {
            return Optional.ofNullable((T) field.get(obj));
        } catch (IllegalAccessException e) {
            System.err.println("Failed to get field value: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 객체의 필드 값을 설정합니다.
     *
     * @param obj 필드 값을 설정할 객체
     * @param field 필드 객체
     * @param value 설정할 값
     * @return 성공 시 true, 실패 시 false
     */
    public static boolean setFieldValue(Object obj, Field field, Object value) {
        try {
            field.set(obj, value);
            return true;
        } catch (IllegalAccessException e) {
            System.err.println("Failed to set field value: " + e.getMessage());
            return false;
        }
    }

    /**
     * 클래스에서 특정 이름과 파라미터를 가진 메서드를 가져옵니다. 상위 클래스의 메서드도 탐색합니다.
     *
     * @param clazz 메서드를 찾을 클래스
     * @param methodName 찾을 메서드의 이름
     * @param parameterTypes 메서드의 파라미터 타입들
     * @return 찾은 Method 객체, 없으면 Optional.empty()
     */
    public static Optional<Method> getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                Method method = currentClass.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true); // private 메서드 접근 허용
                return Optional.of(method);
            } catch (NoSuchMethodException e) {
                currentClass = currentClass.getSuperclass(); // 상위 클래스에서 찾기
            }
        }
        return Optional.empty();
    }

    /**
     * 객체에서 메서드를 호출합니다.
     *
     * @param obj 메서드를 호출할 객체
     * @param method 메서드 객체
     * @param args 메서드에 전달할 인자들
     * @param <T> 메서드 반환 값의 타입
     * @return 메서드 호출 결과, 호출 실패 시 Optional.empty()
     */
    public static <T> Optional<T> invokeMethod(Object obj, Method method, Object... args) {
        try {
            return Optional.ofNullable((T) method.invoke(obj, args));
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.err.println("Failed to invoke method: " + e.getMessage());
            return Optional.empty();
        }
    }
}
