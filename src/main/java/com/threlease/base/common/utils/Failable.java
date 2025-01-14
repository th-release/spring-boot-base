package com.threlease.base.common.utils;

import lombok.Getter;

@Getter
public class Failable<T, E> {
    private boolean isError;
    private T value;
    private E error;

    private Failable(T value, E error, boolean isError) {
        this.value = value;
        this.error = error;
        this.isError = isError;
    }

    public static <T, E> Failable<T, E> success(T value) {
        return new Failable<>(value, null, false);
    }

    public static <T, E> Failable<T, E> error(E error) {
        return new Failable<>(null, error, true);
    }
}
