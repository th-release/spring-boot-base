package com.threlease.base.functions.auth.dto;

import com.threlease.base.common.annotation.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SignUpDto {
    @NotBlank
    @Size(max = 24)
    private String username;

    @NotBlank
    @Size(max = 36)
    private String nickname;

    @NotBlank
    @ValidEmail
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 8, max = 128)
    private String password;
}
