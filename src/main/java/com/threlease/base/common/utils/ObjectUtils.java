package com.threlease.base.common.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.util.stream.Stream;

/**
 * 객체 관련 유틸리티 메서드를 제공하는 정적 클래스입니다.
 */
public class ObjectUtils {

    private ObjectUtils() {
        // 정적 유틸리티 클래스는 인스턴스화할 수 없습니다.
    }

    /**
     * 원본 객체의 속성을 대상 객체로 복사합니다.
     * Spring의 BeanUtils.copyProperties를 사용하여 속성 복사를 수행합니다.
     * 이 메서드는 기본적으로 null 값을 복사하지 않습니다.
     *
     * @param source 원본 객체
     * @param target 대상 객체
     */
    public static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * 원본 객체에서 null이 아닌 속성 이름들을 가져옵니다.
     * 이는 copyProperties에서 null 값을 복사하지 않도록 할 때 사용됩니다.
     *
     * @param source 원본 객체
     * @return null이 아닌 속성 이름들의 배열
     */
    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }

    /**
     * 주어진 객체가 null인지 확인합니다.
     *
     * @param obj 확인할 객체
     * @return 객체가 null이면 true, 그렇지 않으면 false
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * 주어진 객체가 null이 아닌지 확인합니다.
     *
     * @param obj 확인할 객체
     * @return 객체가 null이 아니면 true, 그렇지 않으면 false
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    /**
     * 두 객체가 동등한지 비교합니다. (null 안전)
     *
     * @param obj1 첫 번째 객체
     * @param obj2 두 번째 객체
     * @return 두 객체가 모두 null이거나 equals 메서드가 true를 반환하면 true
     */
    public static boolean nullSafeEquals(Object obj1, Object obj2) {
        return (obj1 == obj2 || (obj1 != null && obj1.equals(obj2)));
    }

    /**
     * 주어진 객체가 비어있는지 확인합니다. (String, Collection, Map, Array 등)
     *
     * @param obj 확인할 객체
     * @return 객체가 비어있으면 true, 그렇지 않으면 false
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return ((String) obj).isEmpty();
        }
        if (obj instanceof java.util.Collection) {
            return ((java.util.Collection<?>) obj).isEmpty();
        }
        if (obj instanceof java.util.Map) {
            return ((java.util.Map<?, ?>) obj).isEmpty();
        }
        if (obj.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(obj) == 0;
        }
        return false;
    }

    /**
     * 주어진 객체가 비어있지 않은지 확인합니다.
     *
     * @param obj 확인할 객체
     * @return 객체가 비어있지 않으면 true, 그렇지 않으면 false
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }
}
