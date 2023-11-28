package com.threlease.base.functions.auth.dto;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class LoginDto {
    private String username;
    private String password;
}
