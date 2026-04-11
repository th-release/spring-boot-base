package com.threlease.base.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthTypes implements EnumValue {
    GENERAL("일반"),
    INTERNAL("내부"),
    EXTERNAL("외부");

    private final String name;

    @Override
    public String getCode() {
        return name();
    }
}
