package com.threlease.base.common.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 컬렉션 관련 유틸리티 메서드를 제공하는 정적 클래스입니다.
 */
public class CollectionUtils {

    private CollectionUtils() {
        // 정적 유틸리티 클래스는 인스턴스화할 수 없습니다.
    }

    /**
     * 주어진 컬렉션이 null이거나 비어있는지 확인합니다.
     *
     * @param collection 확인할 컬렉션
     * @return 컬렉션이 null이거나 비어있으면 true, 그렇지 않으면 false
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 주어진 컬렉션이 null이 아니고 비어있지 않은지 확인합니다.
     *
     * @param collection 확인할 컬렉션
     * @return 컬렉션이 null이 아니고 비어있지 않으면 true, 그렇지 않으면 false
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * 주어진 맵이 null이거나 비어있는지 확인합니다.
     *
     * @param map 확인할 맵
     * @return 맵이 null이거나 비어있으면 true, 그렇지 않으면 false
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 주어진 맵이 null이 아니고 비어있지 않은지 확인합니다.
     *
     * @param map 확인할 맵
     * @return 맵이 null이 아니고 비어있지 않으면 true, 그렇지 않으면 false
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * null로부터 안전한 컬렉션을 반환합니다. 입력 컬렉션이 null이면 빈 불변 컬렉션을 반환합니다.
     *
     * @param collection null일 수 있는 컬렉션
     * @param <T> 컬렉션 요소의 타입
     * @return null이 아닌 컬렉션 (입력이 null이면 빈 불변 컬렉션)
     */
    public static <T> Collection<T> nullSafe(Collection<T> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }

    /**
     * null로부터 안전한 리스트를 반환합니다. 입력 리스트가 null이면 빈 불변 리스트를 반환합니다.
     *
     * @param list null일 수 있는 리스트
     * @param <T> 리스트 요소의 타입
     * @return null이 아닌 리스트 (입력이 null이면 빈 불변 리스트)
     */
    public static <T> List<T> nullSafe(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * null로부터 안전한 셋을 반환합니다. 입력 셋이 null이면 빈 불변 셋을 반환합니다.
     *
     * @param set null일 수 있는 셋
     * @param <T> 셋 요소의 타입
     * @return null이 아닌 셋 (입력이 null이면 빈 불변 셋)
     */
    public static <T> Set<T> nullSafe(Set<T> set) {
        return set == null ? Collections.emptySet() : set;
    }

    /**
     * 컬렉션을 스트림을 사용하여 새로운 리스트로 변환합니다.
     *
     * @param collection 변환할 컬렉션
     * @param mapper 각 요소를 변환하는 함수
     * @param <T> 원본 컬렉션 요소의 타입
     * @param <R> 새 리스트 요소의 타입
     * @return 변환된 요소로 구성된 새 리스트
     */
    public static <T, R> List<R> mapToList(Collection<T> collection, Function<? super T, ? extends R> mapper) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * 컬렉션을 스트림을 사용하여 새로운 셋으로 변환합니다.
     *
     * @param collection 변환할 컬렉션
     * @param mapper 각 요소를 변환하는 함수
     * @param <T> 원본 컬렉션 요소의 타입
     * @param <R> 새 셋 요소의 타입
     * @return 변환된 요소로 구성된 새 셋
     */
    public static <T, R> Set<R> mapToSet(Collection<T> collection, Function<? super T, ? extends R> mapper) {
        if (isEmpty(collection)) {
            return Collections.emptySet();
        }
        return collection.stream().map(mapper).collect(Collectors.toSet());
    }
}
