package com.threlease.base.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserTypes implements EnumValue {
    USER("일반 사용자"),
    ADMIN("관리자 유형"),
    TEACHER("선생님"),
    STUDENT("학생");

    private final String name;

    @Override
    public String getCode() {
        return name();
    }
}
