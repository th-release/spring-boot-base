package com.threlease.base.common.utils.responses;
// compileOnly 'org.projectlombok:lombok'
// annotationProcessor 'org.projectlombok:lombok'
// com.google.gson.Gson

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import com.google.gson.Gson;

import java.util.Optional;

@Data
@Getter
@Setter
@Builder
public class BasicResponse<T> {
    private boolean success;
    private Optional<String> message;
    private Optional<T> data;

    public String toJson() {
        Gson gson = new Gson();

        return gson.toJson(this);
    }
}
