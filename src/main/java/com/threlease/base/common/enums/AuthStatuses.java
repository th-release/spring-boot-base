package com.threlease.base.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthStatuses implements EnumValue {
    ACTIVE("활성"),
    LOCKED("잠금"),
    SUSPENDED("정지"),
    WITHDRAWN("탈퇴");

    private final String name;

    @Override
    public String getCode() {
        return name();
    }
}
