package com.threlease.base.functions.auth;

import com.threlease.base.common.annotation.RateLimit;
import com.threlease.base.common.enums.Roles;
import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.crypto.Hash;
import com.threlease.base.common.utils.random.GetRandom;
import com.threlease.base.common.utils.random.RandomType;
import com.threlease.base.common.utils.responses.BasicResponse;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.dto.LoginDto;
import com.threlease.base.functions.auth.dto.SignUpDto;
import com.threlease.base.functions.auth.dto.TokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth API")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @RateLimit(limit = 10, window = 60)
    @Operation(summary = "로그인")
    public ResponseEntity<BasicResponse<TokenResponseDto>> login(
            @RequestBody @Valid LoginDto dto
    ) {
        AuthEntity auth = authService.findOneByUsername(dto.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!auth.getPassword().equals(Hash.generateSHA512(dto.getPassword() + auth.getSalt()))) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        return BasicResponse.created(authService.issueTokens(auth));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급")
    public ResponseEntity<BasicResponse<TokenResponseDto>> refresh(
            @RequestHeader("Refresh-Token") String refreshToken
    ) {
        return BasicResponse.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/signup")
    @RateLimit(limit = 5, window = 60)
    @Operation(summary = "회원가입")
    public ResponseEntity<BasicResponse<AuthEntity>> signUp(
            @RequestBody @Valid SignUpDto dto
    ) {
        if (authService.findOneByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE);
        }

        String salt = GetRandom.run(RandomType.ALL, 32);

        AuthEntity user = AuthEntity.builder()
                .username(dto.getUsername())
                .password(Hash.generateSHA512(dto.getPassword() + salt))
                .salt(salt)
                .role(Roles.ROLE_USER)
                .build();

        authService.authSave(user);

        return BasicResponse.created(user);
    }

    @GetMapping("/@me")
    @Operation(summary = "내 정보 조회")
    public ResponseEntity<BasicResponse<AuthEntity>> me(
            @RequestHeader("Authorization") String token
    ) {
        AuthEntity user = authService.findOneByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

        return BasicResponse.ok(user);
    }
}