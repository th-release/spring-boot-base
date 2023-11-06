package com.threlease.base.utils.responses;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Data
@Getter
@Setter
@Builder
public class BasicResponse {
    private boolean success;
    private Optional<String> message;
    private Optional<Object> data;
}
