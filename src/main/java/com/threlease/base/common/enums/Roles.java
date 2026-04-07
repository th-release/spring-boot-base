package com.threlease.base.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Roles implements EnumValue {
    ROLE_USER("일반 사용자"),
    ROLE_ADMIN("관리자");

    private final String name;

    @Override
    public String getCode() {
        return name();
    }
}
