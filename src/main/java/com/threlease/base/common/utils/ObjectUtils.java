package com.threlease.base.common.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 객체 관련 유틸리티 메서드를 제공하는 정적 클래스입니다.
 */
public class ObjectUtils {

    private ObjectUtils() {
        // 정적 유틸리티 클래스는 인스턴스화할 수 없습니다.
    }

    /**
     * 원본 객체의 null이 아닌 속성만 대상 객체로 복사합니다. (Merge/Patch 용)
     *
     * @param source 원본 객체
     * @param target 대상 객체
     */
    public static void copyNonNullProperties(Object source, Object target) {
        if (source == null || target == null) return;
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * 두 객체 간에 데이터가 변경되었는지 확인합니다. (Diff 용)
     * 모든 필드를 equals로 비교하며, 하나라도 다르면 true를 반환합니다.
     */
    public static boolean isChanged(Object oldObj, Object newObj) {
        if (oldObj == newObj) return false;
        if (oldObj == null || newObj == null) return true;
        if (!oldObj.getClass().equals(newObj.getClass())) return true;

        final BeanWrapper oldWrapper = new BeanWrapperImpl(oldObj);
        final BeanWrapper newWrapper = new BeanWrapperImpl(newObj);

        return Stream.of(oldWrapper.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(name -> !"class".equals(name))
                .anyMatch(name -> !Objects.equals(oldWrapper.getPropertyValue(name), newWrapper.getPropertyValue(name)));
    }

    /**
     * 원본 객체에서 null인 속성 이름들을 가져옵니다.
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
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * 주어진 객체가 null이 아닌지 확인합니다.
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    /**
     * 두 객체가 동등한지 비교합니다. (null 안전)
     */
    public static boolean nullSafeEquals(Object obj1, Object obj2) {
        return Objects.equals(obj1, obj2);
    }

    /**
     * 주어진 객체가 비어있는지 확인합니다. (String, Collection, Map, Array 등)
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) return true;
        if (obj instanceof String) return ((String) obj).isEmpty();
        if (obj instanceof java.util.Collection) return ((java.util.Collection<?>) obj).isEmpty();
        if (obj instanceof java.util.Map) return ((java.util.Map<?, ?>) obj).isEmpty();
        if (obj.getClass().isArray()) return java.lang.reflect.Array.getLength(obj) == 0;
        return false;
    }

    /**
     * 주어진 객체가 비어있지 않은지 확인합니다.
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }
}
