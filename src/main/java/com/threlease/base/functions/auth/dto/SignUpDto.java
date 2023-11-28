package com.threlease.base.functions.auth.dto;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SignUpDto {
    private String username;
    private String nickname;
    private String password;
}
