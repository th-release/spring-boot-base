package com.threlease.base.functions.auth.dto;

import com.threlease.base.common.annotation.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SignUpDto {
    private String username;
    private String nickname;
    @NotBlank
    @ValidEmail
    private String email;
    private String password;
}
