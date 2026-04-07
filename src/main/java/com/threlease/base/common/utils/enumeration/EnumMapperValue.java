package com.threlease.base.common.utils.enumeration;

import com.threlease.base.common.enums.EnumValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum 데이터를 프론트엔드용 DTO로 변환하는 클래스
 */
@Getter
public class EnumMapperValue {
    private final String code;
    private final String name;

    public EnumMapperValue(EnumValue enumValue) {
        this.code = enumValue.getCode();
        this.name = enumValue.getName();
    }
}
