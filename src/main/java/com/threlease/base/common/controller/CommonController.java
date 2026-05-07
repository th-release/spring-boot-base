package com.threlease.base.common.controller;

import com.threlease.base.common.enums.EnumValue;
import com.threlease.base.common.utils.enumeration.EnumMapperValue;
import com.threlease.base.common.utils.responses.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.beans.Introspector;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 공통 코드(Enum) 조회를 위한 컨트롤러
 */
@RestController
@RequestMapping("/common")
@Tag(name = "Common API", description = "공통 코드 및 설정 정보 API")
public class CommonController {
    private static final String ENUM_PACKAGE = "com.threlease.base.common.enums";

    private final Map<String, List<EnumMapperValue>> enumMappings;

    public CommonController() {
        this.enumMappings = discoverEnumMappings();
    }

    @GetMapping("/enums")
    @Operation(summary = "전체 Enum 코드 목록 조회", description = "시스템에서 사용하는 모든 Enum 코드와 명칭을 반환합니다.")
    public ResponseEntity<BasicResponse<Map<String, List<EnumMapperValue>>>> getEnums() {
        return BasicResponse.ok(enumMappings);
    }

    private Map<String, List<EnumMapperValue>> discoverEnumMappings() {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(Enum.class));

        return provider.findCandidateComponents(ENUM_PACKAGE).stream()
                .map(BeanDefinition::getBeanClassName)
                .filter(className -> className != null && !className.equals(EnumValue.class.getName()))
                .map(this::loadClass)
                .filter(this::isExposableEnum)
                .sorted(Comparator.comparing(Class::getSimpleName))
                .collect(Collectors.toMap(
                        this::toEnumKey,
                        this::toEnumValues,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Class<?> loadClass(String className) {
        try {
            return ClassUtils.forName(className, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Enum class could not be loaded: " + className, e);
        }
    }

    private boolean isExposableEnum(Class<?> clazz) {
        return clazz != null
                && clazz.isEnum()
                && clazz.getPackageName().startsWith(ENUM_PACKAGE);
    }

    private String toEnumKey(Class<?> enumClass) {
        return Introspector.decapitalize(enumClass.getSimpleName());
    }

    private List<EnumMapperValue> toEnumValues(Class<?> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(this::toEnumValue)
                .collect(Collectors.toList());
    }

    private EnumMapperValue toEnumValue(Object enumConstant) {
        Enum<?> value = (Enum<?>) enumConstant;
        if (value instanceof EnumValue exposedValue) {
            return new EnumMapperValue(exposedValue);
        }
        return new EnumMapperValue(value.name(), value.name());
    }
}
